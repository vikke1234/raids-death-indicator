package com.example.raids;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import com.example.enemydata.toa.het.Akkha;
import com.example.utils.DamageHandler;
import com.example.utils.TriFunction;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
@Slf4j
public class Toa {
    @Inject
    private Client client;

    @Inject
    private DamageHandler damageHandler;

    public static boolean isAtToa(Client client) {
        final int []TOA_REGIONS = {
                //13455, // Lobby
                //14160, // Nexus

                //15698, // Crondis
                //15700, // Zebak

                //14162, // Scabaras
                14164, // Kephri

                15186, // Apmken
                //15188, // Baba

                14674, // Het
                14676, // Akkha

                //15184, // Wardens
                //15696, // Wardens

                //14672, // Tomb
        };

        return Arrays.stream(client.getTopLevelWorldView().getMapRegions()).anyMatch(current -> Arrays.stream(TOA_REGIONS).anyMatch(reg -> reg == current));
    }

    public boolean isAtToa() {
        return isAtToa(client);
    }

    /**
     * @return TOA invocation level
     */
    private int getInvocation() {
        return client.getVarbitValue(Varbits.TOA_RAID_LEVEL);
    }

    /**
     * Fetches the path level from the TOA widget
     * @return Path level
     */
    private int getPathLevel() {
        Widget pathLevelWidget = client.getWidget(481, 45);
        if (pathLevelWidget == null) {
            return -1;
        }
        return Integer.parseInt(pathLevelWidget.getText());
    }

    /**
     * Gets amount of people in the raid, amount makes the enemy health scale with 90%
     * @return Amount of people in the raid
     */
    private int getPartySize()
    {
        int partySize = 1;
        for (int i = 1; i < 8; i++) {
            if (client.getVarbitValue(Varbits.TOA_MEMBER_0_HEALTH + i) != 0) {
                partySize++;
            }
        }
        return partySize;
    }

    private boolean partyDead() {

        for (int i = Varbits.TOA_MEMBER_0_HEALTH; i <= Varbits.TOA_MEMBER_7_HEALTH; i++) {
            int value = client.getVarbitValue(i);
            if (value != 30 && value != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Create the new Akkha object when the TOA path widget has loaded, we aren't able to get the path level otherwise.
     * @param event
     */
    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        // 481 is the TOA path/time/etc. widget
        if (event.getGroupId() != InterfaceID.TOA_RAID) {
            return;
        }
        int invo = getInvocation();
        int pathLevel = getPathLevel();
        int partySize = getPartySize();
        if (pathLevel < 0 || invo < 0 || partySize <= 0) {
            return;
        }
        for (Enemy enemy : damageHandler.getActiveEnemies().values()) {
            if (enemy instanceof ToaEnemy) {
                ToaEnemy e = (ToaEnemy) enemy;
                e.fixupStats(invo, partySize, pathLevel);
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        if (partyDead()) {
            // TODO: does this need to be done each tick?
            damageHandler.getActiveEnemies().clear();
        }
    }

    @Subscribe
    protected void onNpcDespawned(NpcDespawned event) {
        if (!isAtToa()) {
            return; // only handle toa enemies
        }
        NPC npc = event.getNpc();
        boolean isAkkha = Akkha.isAkkha(npc.getId());
        boolean isPartyDead = partyDead();
        if (isAkkha && !isPartyDead) {
            return;
        }
        damageHandler.getActiveEnemies().remove(npc.getIndex());
    }

    @Subscribe
    protected void onNpcSpawned(NpcSpawned event) {
        if (!isAtToa()) {
            return;
        }
        var activeEnemies = damageHandler.getActiveEnemies();
        NPC npc = event.getNpc();
        if (activeEnemies.containsKey(npc.getIndex())) {
            Enemy enemy = activeEnemies.get(npc.getIndex());
            enemy.setNpc(npc);
            // Re-sync the health of the enemy (akkha) when it re-appears in case of hits during invuln
            // TODO should akkha be subscribed to the event bus?
            int newHealth = client.getVarbitValue(Varbits.BOSS_HEALTH_CURRENT);
            enemy.setCurrentHealth(newHealth);
            enemy.setQueuedDamage(0);

            return;
        }

        TriFunction<NPC, Integer, Integer, Integer, Enemy> constructor = ToaEnemy.enemies.getOrDefault(npc.getId(), null);
        if (constructor == null) {
            return;
        }

        Enemy enemy = constructor.apply(npc, getInvocation(), getPartySize(), getPathLevel());
        enemy.setClient(client);

        activeEnemies.put(npc.getIndex(), enemy);
    }
}
