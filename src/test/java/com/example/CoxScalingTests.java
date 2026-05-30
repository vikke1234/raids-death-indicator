package com.example;

import com.example.enemydata.Enemy;
import com.example.enemydata.cox.CoxEnemy;
import com.example.enemydata.cox.SkeletalMystic;
import com.example.enemydata.toa.ToaEnemy;
import com.example.utils.PentFunction;
import com.example.utils.QuadFunction;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class CoxScalingTests {
    private static final float delta = 0.00000001F;

    @Test
    public void testMysticScaling() {
        TestNPC npc = new TestNPC(NpcID.SKELETAL_MYSTIC);
        SkeletalMystic mystic = new SkeletalMystic(npc, true, 3, 126, 99);
        assertEquals(480, mystic.scaledHealth);
    }

    /**
     * Catalogue every modifier produced by every TOA and COX enemy at a range
     * of common raid configs, then print scaling → {npcId, config} for the
     * ones in the 1.000..1.325 range we care about.
     */
    @Test
    public void enumerateLowScalings() {
        Map<Double, Set<String>> bucket = new TreeMap<>();

        // TOA: invo 0..600 step 50, partySize 1..8, pathLevel 0..6.
        for (Map.Entry<Integer, QuadFunction<NPC, Integer, Integer, Integer, Enemy>> e
                : ToaEnemy.enemies.entrySet()) {
            int id = e.getKey();
            TestNPC npc = new TestNPC(id);
            String name = "toa#" + id;
            for (int invo = 0; invo <= 600; invo += 50) {
                for (int party = 1; party <= 8; party++) {
                    for (int path = 0; path <= 6; path++) {
                        try {
                            Enemy enemy = e.getValue().apply(npc, invo, party, path);
                            record(bucket, enemy.getModifier(),
                                    name + " invo=" + invo + " p=" + party + " path=" + path);
                        } catch (Throwable t) {
                            // Skip configs the constructor rejects (e.g. assertion mismatches).
                        }
                    }
                }
            }
        }

        // COX: isCm true/false, partySize 1..8, maxCombat 100/110/126, maxHp 70/85/99.
        for (Map.Entry<Integer, PentFunction<NPC, Boolean, Integer, Integer, Integer, CoxEnemy>> e
                : CoxEnemy.enemies.entrySet()) {
            int id = e.getKey();
            TestNPC npc = new TestNPC(id);
            String name = "cox#" + id;
            for (boolean cm : new boolean[]{false, true}) {
                for (int party = 1; party <= 8; party++) {
                    for (int maxCombat : new int[]{100, 110, 126}) {
                        for (int maxHp : new int[]{70, 85, 99}) {
                            try {
                                CoxEnemy enemy = e.getValue().apply(npc, cm, party, maxCombat, maxHp);
                                record(bucket, enemy.getModifier(),
                                        name + " cm=" + cm + " p=" + party + " cb=" + maxCombat + " hp=" + maxHp);
                            } catch (Throwable t) {
                                // skip
                            }
                        }
                    }
                }
            }
        }

        double[] interesting = {1.000, 1.025, 1.050, 1.075, 1.100, 1.125, 1.150,
                1.175, 1.200, 1.225, 1.250, 1.275, 1.300, 1.325};
        for (double s : interesting) {
            Set<String> who = bucket.getOrDefault(s, java.util.Collections.emptySet());
            // Print a sample (first 6) to keep output manageable.
            int shown = 0;
            System.out.printf("scaling=%.3f (%d configs):%n", s, who.size());
            for (String w : who) {
                System.out.printf("  %s%n", w);
                if (++shown >= 6) {
                    if (who.size() > shown) {
                        System.out.printf("  ... and %d more%n", who.size() - shown);
                    }
                    break;
                }
            }
        }
    }

    private static void record(Map<Double, Set<String>> bucket, double scaling, String label) {
        bucket.computeIfAbsent(scaling, k -> new TreeSet<>()).add(label);
    }

    /**
     * Print the Akkha modifier across invocation 300..500 and party 1..8,
     * for each path level. Helps spot which configs land on the predictor's
     * ambiguous scaling bands.
     */
    @Test
    public void akkhaModifierGrid() {
        TestNPC npc = new TestNPC(NpcID.AKKHA_11790);
        int[] invos = {300, 325, 350, 375, 400, 425, 450, 475, 500};
        for (int path = 0; path <= 6; path++) {
            System.out.printf("%n=== path level %d ===%n", path);
            System.out.printf("%-7s", "invo");
            for (int p = 1; p <= 8; p++) {
                System.out.printf(" p%d-----", p);
            }
            System.out.println();
            for (int invo : invos) {
                System.out.printf("%-7d", invo);
                for (int p = 1; p <= 8; p++) {
                    try {
                        com.example.enemydata.toa.het.Akkha akkha =
                                new com.example.enemydata.toa.het.Akkha(npc, invo, p, path);
                        System.out.printf(" %-7.3f", akkha.getModifier());
                    } catch (Throwable t) {
                        System.out.printf(" %-7s", "err");
                    }
                }
                System.out.println();
            }
        }
    }
}
