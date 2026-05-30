package com.example;

import com.example.enemydata.Enemy;
import com.example.enemydata.cox.CoxEnemy;
import com.example.enemydata.cox.SkeletalMystic;
import com.example.enemydata.toa.ToaEnemy;
import com.example.utils.PentFunction;
import com.example.utils.QuadFunction;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;
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
        TestNPC npc = new TestNPC(NpcID.RAIDS_SKELETONMYSTIC_A);
        SkeletalMystic mystic = new SkeletalMystic(npc, true, 3, 126, 99);
        assertEquals(480, mystic.scaledHealth);
    }

    @Test
    public void testVanguardSoloHp() {
        TestNPC npc = new TestNPC(NpcID.RAIDS_VANGUARD_MELEE);
        com.example.enemydata.cox.Vanguard regular =
                new com.example.enemydata.cox.Vanguard(npc, false, 1, 126, 99);
        assertEquals(180, regular.scaledHealth);

        com.example.enemydata.cox.Vanguard cm =
                new com.example.enemydata.cox.Vanguard(npc, true, 1, 126, 99);
        assertEquals(270, cm.scaledHealth);
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

    @Test
    public void simulateVasaCmSolo() {
        TestNPC npc = new TestNPC(NpcID.RAIDS_VASANISTIRIO_DORMANT);
        com.example.enemydata.cox.VasaNistirio vasa =
                new com.example.enemydata.cox.VasaNistirio(npc, true, 1, 126, 99);
        double scaling = vasa.getModifier();
        com.example.utils.Predictor.Properties props =
                new com.example.utils.Predictor.Properties(net.runelite.api.Skill.HITPOINTS, false, false, scaling);
        com.example.utils.Predictor predictor = new com.example.utils.Predictor();

        int[] openingHits = {98, 61, 21, 8, 8, 36, 65, 50};
        int totalHits = 25;
        int internalFrac = 0; // assume player carry starts at 0
        java.util.Random rng = new java.util.Random(42);

        int exact = 0, under = 0, over = 0;
        int totalDamage = 0;
        System.out.printf("Vasa CM solo simulation (scaling=%.3f, hp=%d)%n", scaling, vasa.scaledHealth);
        System.out.printf("%-4s %-7s %-5s %-9s %-5s%n", "#", "actual", "xp", "predicted", "");

        for (int i = 0; i < totalHits; i++) {
            int hit = i < openingHits.length ? openingHits[i] : 1 + rng.nextInt(90);
            int precise = com.example.utils.Predictor.computePrecise(hit, props);
            int total = precise + internalFrac;
            int displayedXp = total / 10;
            internalFrac = total % 10;

            int predicted = predictor.treePredict(displayedXp, props);
            String tag = predicted == hit ? "exact"
                    : predicted > hit ? "OVER (unsafe!)"
                    : "under";
            if (predicted == hit) exact++;
            else if (predicted > hit) over++;
            else under++;

            totalDamage += hit;
            boolean accurate = predictor.isAccurate(net.runelite.api.Skill.HITPOINTS);
            System.out.printf("%-4d %-7d %-5d %-9d %s%s%n",
                    i + 1, hit, displayedXp, predicted, tag,
                    accurate ? " [calibrated]" : "");
        }

        System.out.printf("%nTotal hits: %d, exact=%d, under=%d, over=%d, total damage=%d%n",
                totalHits, exact, under, over, totalDamage);
    }

}
