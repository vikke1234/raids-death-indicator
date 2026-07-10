package com.example.raids;

import com.example.enemydata.Enemy;
import com.example.enemydata.misc.nightmare.NightmareHuskRanged;
import com.example.enemydata.misc.nightmare.NightmareParasite;
import com.example.enemydata.misc.nightmare.NightmareParasiteEnraged;
import com.example.enemydata.misc.nightmare.NightmareHuskMage;
import com.example.enemydata.misc.nightmare.NightmareTotem;
import com.example.enemydata.misc.nightmare.PhosaniParasite;
import com.example.enemydata.misc.nightmare.PhosaniParasiteEnraged;
import com.example.utils.DamageHandler;
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

    private void addIfValid(NPC npc) {
        if (npc == null) {
            return;
        }

        var activeEnemies = damageHandler.getActiveEnemies();
        int idx = npc.getIndex();

        if (activeEnemies.containsKey(idx)) {
            // Already tracked
            return;
        }

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
            }
        }
    }

    @Subscribe
    protected void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        addIfValid(npc);
    }

    @Subscribe
    protected void onNpcChanged(NpcChanged event) {
        NPC npc = event.getNpc();
        addIfValid(npc);
    }

    @Subscribe
    protected void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        // Remove value if it exists
        damageHandler.getActiveEnemies().remove(npc.getIndex());
    }
}
