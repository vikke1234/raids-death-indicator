package com.example.raids;

import com.example.enemydata.Enemy;
import com.example.enemydata.cox.CoxEnemy;
import com.example.enemydata.cox.Tekton;
import com.example.utils.DamageHandler;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class Cox {
    static class InternalVarbits {
        public static final int COX_CM = 6835;
        public static final int GROUP_SIZE = 9540;
    }

    @Inject
    private DamageHandler damageHandler;

    @Inject
    private Client client;

    @Inject
    private HiscoreClient hiscoreClient;

    Set<String> cachedPlayers;

    boolean isCm;

    int groupSize;

    int maxCombat;

    int maxHp;

    @Inject
    public void initialize() {
        isCm = false;
        groupSize = 0;
        maxCombat = 0;
        maxHp = 0;
        cachedPlayers = new HashSet<>();
    }

    public static boolean isInCox(Client client) {
        return client.getVarbitValue(Varbits.RAID_STATE) >= 1;
    }

    public boolean isInCox() {
        return isInCox(client);
    }

    private int getMaxHpInRaid() {
        var players = client.getTopLevelWorldView().players().stream().collect(Collectors.toList());
        maxCombat = players.stream().map(Player::getCombatLevel).max(Integer::compare).get();

        // only get the first 5 players so that masses don't ddos hiscore servers.
        var sorted = players.stream()
                .sorted(Comparator.comparingInt((Player p) -> p.getCombatLevel()))
                .collect(Collectors.toList());
        int size = sorted.size();
        System.out.println("Fetching stats: " + sorted);
        List<HiscoreResult> results = sorted.subList(0, Math.min(5, size)).stream().map(p -> {
            try {
                return hiscoreClient.lookup(p.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        System.out.println("Done fetching stats");
        return results.stream()
                .map(result -> result.getSkill(HiscoreSkill.HITPOINTS).getLevel())
                .max(Integer::compare).get();
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged ev) {
        if (ev.getVarbitId() == Varbits.RAID_STATE && ev.getValue() == 1) {
            isCm = client.getVarbitValue(InternalVarbits.COX_CM) == 1;
            groupSize = client.getVarbitValue(InternalVarbits.GROUP_SIZE);
            maxHp = getMaxHpInRaid();
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned ev) {
        if (!isInCox()) {
            return;
        }
        NPC npc = ev.getNpc();
        var activeEnemies = damageHandler.getActiveEnemies();
        System.out.println(MessageFormat.format("NPC despawned \"{0}\": {1} {2}", npc.getName(), npc.getId(), npc.getIndex()));

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
        System.out.println(MessageFormat.format("NPC spawned \"{0}\": {1} {2}", npc.getName(), npc.getId(), npc.getIndex()));
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
