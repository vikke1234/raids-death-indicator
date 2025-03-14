package com.example.raids;

import com.example.AkkhaPredictorConfig;
import com.example.enemydata.Enemy;
import com.example.enemydata.cox.CoxEnemy;
import com.example.enemydata.cox.Tekton;
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
    static class InternalVarbits {
        public static final int COX_CM = 6385;
        public static final int GROUP_SIZE = 9540;
    }

    @Inject
    private DamageHandler damageHandler;

    @Inject
    private Client client;

    @Inject
    private HiscoreClient hiscoreClient;

    @Inject
    private AkkhaPredictorConfig config;

    @Getter
    AtomicBoolean cachedInCox;

    boolean isCm;

    int groupSize;

    int maxCombat;

    int maxHp;

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
        int state = client.getVarbitValue(Varbits.RAID_STATE);
        return state >= 1 && state < 5;
    }

    public boolean isInCox() {
        return isInCox(client) && config.enableCox(); // TODO: find a better way to check if in cox
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged ev) {
        if (ev.getVarbitId() == Varbits.RAID_STATE) {
            if (ev.getValue() == 1) {
                isCm = client.getVarbitValue(InternalVarbits.COX_CM) == 1;
                groupSize = client.getVarbitValue(InternalVarbits.GROUP_SIZE);
                maxHp = config.maxHp();
                maxCombat = client.getTopLevelWorldView().players().stream().map(Player::getCombatLevel).max(Integer::compare).get();
            }
            cachedInCox.set(ev.getValue() >= 1 && ev.getValue() < 5);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned ev) {
        if (!isInCox()) {
            return;
        }
        NPC npc = ev.getNpc();
        var activeEnemies = damageHandler.getActiveEnemies();
        //System.out.println(MessageFormat.format("NPC despawned \"{0}\": {1} {2}", npc.getName(), npc.getId(), npc.getIndex()));

        activeEnemies.remove(npc.getIndex());
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned ev) {
        if (!isInCox()) {
            return;
        }

        NPC npc = ev.getNpc();
        var enemyConstructor = CoxEnemy.enemies.getOrDefault(npc.getId(), null);
        if (enemyConstructor == null) {
            return;
        }
        //System.out.println(MessageFormat.format("NPC spawned \"{0}\": {1} {2}", npc.getName(), npc.getId(), npc.getIndex()));
        Enemy enemy = enemyConstructor.apply(npc, isCm, groupSize, maxCombat, maxHp);
        damageHandler.getActiveEnemies().put(npc.getIndex(), enemy);
    }

    @Subscribe
    public void onNpcChanged(NpcChanged ev) {
        if (!isInCox()) {
            return;
        }

        NPC npc = ev.getNpc();
        int id = npc.getId();
        Enemy enemy = damageHandler.getActiveEnemies().getOrDefault(npc.getIndex(), null);
        if (!(enemy instanceof Tekton)) {
            return;
        }

        Tekton tekton = (Tekton) enemy;
        switch (id) {
            case NpcID.TEKTON:
            case NpcID.TEKTON_7541:
            case NpcID.TEKTON_7542:
            case NpcID.TEKTON_7545:
                tekton.swapForm(false);
                break;

            case NpcID.TEKTON_ENRAGED:
            case NpcID.TEKTON_ENRAGED_7544:
                tekton.swapForm(true);
                break;
        }
    }
}
