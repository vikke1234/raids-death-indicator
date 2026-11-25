package com.example.raids;

import net.runelite.api.gameval.*;
import net.runelite.api.gameval.NpcID;

import com.example.generated.EnemySpawnContext;
import com.example.generated.GeneratedEnemyConfig;
import com.example.generated.TrackedEnemy;
import com.example.enemydata.*;
import com.example.enemydata.cox.CoxEnemy;
import com.example.utils.DamageHandler;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.hiscore.HiscoreClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class Cox {
    @Inject
    private DamageHandler damageHandler;

    @Inject
    private Client client;

    @Inject
    private HiscoreClient hiscoreClient;

    @Inject
    private GeneratedEnemyConfig config;

    @Getter
    AtomicBoolean cachedInCox;

    boolean isCm;

    int groupSize;

    int maxCombat;

    int maxHp;

    /**
     * Ticks since raid started, used to account for regen.
     */
    long raidTick;

    @Inject
    public void initialize() {
        cachedInCox = new AtomicBoolean();
        cachedInCox.set(false);
        isCm = false;
        groupSize = 0;
        maxCombat = 0;
        maxHp = 0;
    }

    public static boolean isInCox(Client client) {
        int state = client.getVarbitValue(VarbitID.RAIDS_CLIENT_INDUNGEON);
        return state == 1;
    }

    public boolean isInCox() {
        return isInCox(client);
    }

    @Subscribe
    public void onGameTick(GameTick ev) {
        if (!isInCox()) {
            return;
        }

        for (Enemy e : damageHandler.getActiveEnemies().values()) {
            if (e instanceof CoxEnemy) {
                CoxEnemy coxEnemy = (CoxEnemy) e;
                coxEnemy.incrementRaidTick();
            }
        }

        if (raidTick == Long.MAX_VALUE) {
            raidTick = 0;
        } else {
            raidTick++;
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged ev) {
        switch (ev.getVarbitId()) {
            case VarbitID.RAIDS_CLIENT_PROGRESS:
                if (ev.getValue() == 1) {
                    boolean isLeader = client.getVarbitValue(VarbitID.RAIDS_CLIENT_ISLEADER) != 0;
                    isCm = isLeader
                            ? client.getVarbitValue(VarbitID.RAIDS_CHALLENGE_MODE) != 0
                            : config.isCM();
                    groupSize = client.getVarbitValue(VarbitID.RAIDS_CLIENT_PARTYSIZE_SCALED);
                    maxHp = config.maxHp();
                    maxCombat = client.getServerVarbitValue(VarbitID.RAIDS_CLIENT_HIGHESTCOMBAT);
                    raidTick = 0;
                }
                cachedInCox.set(ev.getValue() >= 1 && ev.getValue() < 5);
                break;
            case VarbitID.RAIDS_CLIENT_INDUNGEON:
                if (ev.getValue() == 0) {
                    damageHandler.getActiveEnemies().clear();
                }
                break;

        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned ev) {
        if (!isInCox()) {
            return;
        }
        NPC npc = ev.getNpc();
        var activeEnemies = damageHandler.getActiveEnemies();
        activeEnemies.remove(npc.getIndex());
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned ev) {
        if (!isInCox()) {
            return;
        }
        trackIfTrackable(ev.getNpc());
    }

    /**
     * Some COX NPCs (e.g. Vanguards) spawn under one id and transition to a
     * different id via NpcChanged. If only the post-change id is in our
     * registry, the spawn handler would miss them — handle the change too.
     */
    @Subscribe
    public void onNpcChanged(NpcChanged ev) {
        if (!isInCox()) {
            return;
        }
        NPC npc = ev.getNpc();
        trackIfTrackable(npc);

        var activeEnemies = damageHandler.getActiveEnemies();
        CoxEnemy enemy;

        switch (npc.getId()) {
            case NpcID.RAIDS_STONEGUARDIANS_RIGHT_DEAD:
            case NpcID.RAIDS_STONEGUARDIANS_LEFT_DEAD:
                activeEnemies.remove(npc.getIndex());
                break;

            case NpcID.RAIDS_VANGUARD_WALKING:
                enemy = (CoxEnemy) activeEnemies.get(npc.getIndex());
                if (enemy != null) {
                    enemy.heal(1);
                    enemy.setStalled(true);
                }
                break;

            case NpcID.RAIDS_VANGUARD_RANGED:
            case NpcID.RAIDS_VANGUARD_MAGIC:
            case NpcID.RAIDS_VANGUARD_MELEE:
                enemy = (CoxEnemy) activeEnemies.get(npc.getIndex());
                if (enemy != null) {
                    enemy.setStalled(false);
                }
                break;
        }
    }

    private void trackIfTrackable(NPC npc) {
        var activeEnemies = damageHandler.getActiveEnemies();
        if (activeEnemies.containsKey(npc.getIndex())) {
            // Already tracking this NPC index — id change at the same index is
            // typically a state transition (e.g. Vanguard sleep → combat), the
            // existing Enemy keeps the live npc reference.
            return;
        }

        // TODO: Add support for guardians in non-solo
        if ((npc.getId() == NpcID.RAIDS_STONEGUARDIANS_RIGHT ||
                npc.getId() == NpcID.RAIDS_STONEGUARDIANS_LEFT) && groupSize != 1) {
            return;
        }

        Enemy enemy = TrackedEnemy.forNpcId(npc.getId())
                .map(e -> e.create(npc, EnemySpawnContext.cox(isCm, groupSize, maxCombat, maxHp, raidTick)))
                .orElse(null);
        if (enemy == null) {
            return;
        }
        activeEnemies.put(npc.getIndex(), enemy);
    }
}
