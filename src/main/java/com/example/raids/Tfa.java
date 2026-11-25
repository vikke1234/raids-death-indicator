package com.example.raids;

import com.example.enemydata.Enemy;
import com.example.enemydata.tfa.ArchiveGuardian;
import com.example.enemydata.tfa.InertGuardian;
import com.example.enemydata.tfa.TfaEnemy;
import com.example.utils.DamageHandler;
import com.example.utils.EnemyTrackingConfig;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.function.Function;

@Singleton
public class Tfa {
    @Inject
    private Client client;

    @Inject
    private DamageHandler damageHandler;

    @Inject
    private EnemyTrackingConfig enemyTrackingConfig;

    private static final int[] TFA_REGIONS = {
            16471,
            16216,
            16214,
            16727,
            16728,
            16726,
            16215,
    };

    public static boolean isAtTfa(Client client) {
        return Arrays.stream(client.getTopLevelWorldView().getMapRegions())
                .anyMatch(region -> Arrays.stream(TFA_REGIONS).anyMatch(r -> r == region));
    }

    public boolean isAtTfa() {
        return isAtTfa(client);
    }

    @Subscribe
    protected void onNpcSpawned(NpcSpawned event) {
        if (!isAtTfa()) {
            return;
        }
        NPC npc = event.getNpc();
        var activeEnemies = damageHandler.getActiveEnemies();
        int idx = npc.getIndex();

        if (activeEnemies.containsKey(idx)) {
            Enemy enemy = activeEnemies.get(idx);
            enemy.setNpc(npc);
            enemy.setQueuedDamage(0);
            return;
        }

        Function<NPC, Enemy> constructor = TfaEnemy.enemies.get(npc.getId());
        if (constructor == null || !enemyTrackingConfig.isTracked(npc.getId())) {
            return;
        }

        Enemy enemy = constructor.apply(npc);
        if (enemy instanceof ArchiveGuardian) {
            ((ArchiveGuardian) enemy).syncFromClient(client);
        }
        enemy.setClient(client);
        activeEnemies.put(idx, enemy);
    }

    /**
     * Wall guardians do not die — they change to their destroyed composition.
     * The boss also changes id between its shielded and unshielded states.
     */
    @Subscribe
    protected void onNpcChanged(NpcChanged event) {
        if (!isAtTfa()) {
            return;
        }
        NPC npc = event.getNpc();
        int oldId = event.getOld().getId();
        int newId = npc.getId();
        var activeEnemies = damageHandler.getActiveEnemies();

        if (InertGuardian.wasDestroyed(oldId, newId)) {
            ArchiveGuardian guardian = activeEnemies.values().stream()
                    .filter(ArchiveGuardian.class::isInstance)
                    .map(ArchiveGuardian.class::cast)
                    .findFirst()
                    .orElse(null);
            if (guardian != null) {
                guardian.onInertGuardianDestroyed();
            }
            return;
        }

        if (ArchiveGuardian.isGuardian(oldId) && ArchiveGuardian.isGuardian(newId) && oldId != newId) {
            Enemy enemy = activeEnemies.get(npc.getIndex());
            if (enemy instanceof ArchiveGuardian) {
                ((ArchiveGuardian) enemy).onNpcChanged(oldId, newId, client);
            }
        }
    }

    @Subscribe
    protected void onNpcDespawned(NpcDespawned event) {
        if (!isAtTfa()) {
            return;
        }
        NPC npc = event.getNpc();
        damageHandler.getActiveEnemies().remove(npc.getIndex());
    }
}
