package com.example.enemydata;

import com.example.utils.TriFunction;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

import java.util.HashMap;
import java.util.Map;

public abstract class Enemy implements IEnemy {
    public static Map<Integer, TriFunction<NPC, Integer, Integer, Integer, Enemy>> enemies;
    final NPCStats stats;

    @Getter
    final NPC npc;

    static {
        enemies = new HashMap<>();
        enemies.put(NpcID.AKKHA, Akkha::new);
        enemies.put(NpcID.AKKHA_11790, Akkha::new);
        enemies.put(NpcID.AKKHA_11791, Akkha::new);
        enemies.put(NpcID.AKKHA_11792, Akkha::new);
        enemies.put(NpcID.AKKHA_11793, Akkha::new);
        enemies.put(NpcID.AKKHA_11794, Akkha::new);
        enemies.put(NpcID.AKKHA_11795, Akkha::new);
        enemies.put(NpcID.AKKHA_11796, Akkha::new);

        enemies.put(NpcID.AKKHAS_SHADOW, AkkhaShadow::new);

        enemies.put(NpcID.BABA, Baba::new);
        enemies.put(NpcID.BABA_11779, Baba::new);
        enemies.put(NpcID.BABA_11780, Baba::new);

        enemies.put(NpcID.KEPHRI, Kephri::new);
        enemies.put(NpcID.KEPHRI_11720, Kephri::new);
        enemies.put(NpcID.KEPHRI_11721, Kephri::new);
        enemies.put(NpcID.KEPHRI_11722, Kephri::new);

        enemies.put(NpcID.ZEBAK_11730, Zebak::new);
        enemies.put(NpcID.ZEBAK_11732, Zebak::new);
    }

    Enemy(NPC npc, int invocation, int partySize, int pathLevel,
          int baseHealth, int attack, int str, int def,
          int offAtt, int offStr,
          int defStab, int defSlash, int defCrush
    ) {
        assert(partySize <= 8 && partySize >= 1);
        assert((invocation % 5) == 0);
        this.npc = npc;
        if (pathLevel < 0) {
            // Something went wrong with the widget things
            stats = NPCStats.builder(baseHealth)
                    .attack(attack)
                    .str(str)
                    .def(def)
                    .offAtt(offAtt)
                    .offStr(offStr)
                    .defStab(defStab)
                    .defSlash(defSlash)
                    .defCrush(defCrush)
                    .build();
        } else {
            stats = NPCStats.builder(invocation, partySize, pathLevel, baseHealth)
                    .attack(attack)
                    .str(str)
                    .def(def)
                    .offAtt(offAtt)
                    .offStr(offStr)
                    .defStab(defStab)
                    .defSlash(defSlash)
                    .defCrush(defCrush)
                    .build();
        }
    }

    public double getModifier() {
        return stats.getModifier();
    }

    public void hit(int damage) {
        stats.hit(damage);
    }

    public int getQueuedDamage() {
        return stats.queuedDamage;
    }
    public boolean queueDamage(int damage) {
        boolean died = stats.queueDamage(damage);
        npc.setDead(died);
        return died;
    }

    public boolean shouldHighlight() {
        return false;
    }

    public void fixupStats(int invo, int partySize, int pathLevel) {
        // Should only happen before any npcs actually have been damaged
        stats.fixup(invo, partySize, pathLevel);
    }

    public void nearbyDied(NPC npc) {}
}
