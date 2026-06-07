package com.example.raids;

import com.example.enemydata.Enemy;
import com.example.enemydata.misc.NightmareTotem;
import com.example.utils.DamageHandler;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
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

    /**
     * Implicit detection: if any tracked enemy is of a misc-handled type,
     * we're in that encounter. No region or varbit check needed.
     */
    public boolean isAtMiscEncounter() {
        for (Enemy e : damageHandler.getActiveEnemies().values()) {
            if (e instanceof NightmareTotem) {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    protected void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        if (NightmareTotem.NPC_IDS.contains(npc.getId())) {
            damageHandler.getActiveEnemies()
                    .put(npc.getIndex(), new NightmareTotem(npc, client));
        }
    }

    @Subscribe
    protected void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        if (NightmareTotem.NPC_IDS.contains(npc.getId())) {
            damageHandler.getActiveEnemies().remove(npc.getIndex());
        }
    }
}
