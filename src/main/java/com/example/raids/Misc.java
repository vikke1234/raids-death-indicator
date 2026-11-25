package com.example.raids;

import com.example.enemydata.Enemy;
import com.example.enemydata.misc.nightmare.NightmareHuskRanged;
import com.example.enemydata.misc.nightmare.NightmareParasite;
import com.example.enemydata.misc.nightmare.NightmareParasiteEnraged;
import com.example.enemydata.misc.nightmare.NightmareHuskMage;
import com.example.enemydata.misc.AbyssalSire;
import com.example.enemydata.misc.nightmare.NightmareTotem;
import com.example.enemydata.misc.nightmare.PhosaniParasite;
import com.example.enemydata.misc.nightmare.PhosaniParasiteEnraged;
import com.example.utils.DamageHandler;
import com.example.utils.EnemyTrackingConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.events.NpcChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Handler for "miscellaneous" tracked bosses that don't fit under anything
 * really.
 */
@Singleton
@Slf4j
public class Misc {
    @Inject
    private Client client;

    @Inject
    private DamageHandler damageHandler;

    @Inject
    private EnemyTrackingConfig enemyTrackingConfig;

    @Subscribe
    protected void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        if (!enemyTrackingConfig.isTracked(npc.getId())) {
            return;
        }
        var activeEnemies = damageHandler.getActiveEnemies();
        int idx = npc.getIndex();

        if (NightmareTotem.NPC_IDS.contains(npc.getId())) {
            activeEnemies.put(idx, new NightmareTotem(npc, client));
        } else {
            switch (npc.getId()) {
                case NpcID.NIGHTMARE_HUSK_MAGIC:
                    activeEnemies.put(idx, new NightmareHuskMage(npc));
                    break;

                case NpcID.NIGHTMARE_HUSK_RANGED:
                    activeEnemies.put(idx, new NightmareHuskRanged(npc));
                    break;

                case NpcID.NIGHTMARE_PARASITE_WEAK:
                    if (client.getVarbitValue(VarbitID.PLAYER_IS_IN_NIGHTMARE_CHALLENGE) != 0) {
                        activeEnemies.put(idx, new PhosaniParasite(npc));
                    } else {
                        activeEnemies.put(idx, new NightmareParasite(npc));
                    }
                    break;

                case NpcID.NIGHTMARE_PARASITE:
                    if (client.getVarbitValue(VarbitID.PLAYER_IS_IN_NIGHTMARE_CHALLENGE) != 0) {
                        activeEnemies.put(idx, new PhosaniParasiteEnraged(npc));
                    } else {
                        activeEnemies.put(idx, new NightmareParasiteEnraged(npc));
                    }
                    break;

                // Abyssal Sire — phase 1 (spawn, sleeping / awake / stunned)
                case NpcID.ABYSSALSIRE_SIRE_STASIS_SLEEPING:
                case NpcID.ABYSSALSIRE_SIRE_STASIS_AWAKE:
                case NpcID.ABYSSALSIRE_SIRE_STASIS_STUNNED:
                    activeEnemies.putIfAbsent(idx, new AbyssalSire(npc));
                    break;
            }
        }
    }

    @Subscribe
    protected void onNpcChanged(NpcChanged event) {
        var activeEnemies = damageHandler.getActiveEnemies();
        NPC npc = event.getNpc();
        int idx = npc.getIndex();
        int id = npc.getId();
        if (!enemyTrackingConfig.isTracked(id)) {
            return;
        }

        if (NightmareTotem.NPC_IDS.contains(id)) {
            activeEnemies.replace(idx, new NightmareTotem(npc, client));
        }

        // Abyssal Sire — phase transitions
        switch (id) {
            // Phase 2: throne (puppet / wandering)
            case NpcID.ABYSSALSIRE_SIRE_PUPPET:
            case NpcID.ABYSSALSIRE_SIRE_WANDERING:
            case NpcID.ABYSSALSIRE_SIRE_PANICKING:
                // Phase 3 stage 1: off the throne (panicking)
            case NpcID.ABYSSALSIRE_SIRE_APOCALYPSE:
                // Phase 3 stage 2: enrage (apocalypse)
                activeEnemies.putIfAbsent(idx, new AbyssalSire(npc));
                break;
        }
    }

    @Subscribe
    protected void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        // Remove value if it exists
        damageHandler.getActiveEnemies().remove(npc.getIndex());
    }
}
