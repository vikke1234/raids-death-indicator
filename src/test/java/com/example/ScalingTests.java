package com.example;

import com.example.TestNPC;
import com.example.enemydata.toa.ToaEnemy;
import com.example.enemydata.toa.ampken.*;
import com.example.enemydata.toa.het.Akkha;
import com.example.enemydata.toa.scabaras.Kephri;
import com.example.enemydata.toa.scabaras.Kephri721;
import com.example.enemydata.toa.scabaras.Spitter;
import com.example.utils.Predictor;
import net.runelite.api.*;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class ScalingTests {
    private static final float delta = 0.00000001F;
    @Test
    public void testThrowerScaling() {
        TestNPC npc = new TestNPC(NpcID.BABOON_THROWER);
        Thrower thrower = new Thrower(npc, 515, 1, 2);
        assertEquals(1.0, thrower.getModifier(), delta);
        npc = new TestNPC(NpcID.BABOON_THROWER_11713);
        thrower = new Thrower(npc, 515, 1, 2);
        assertEquals(1.0, thrower.getModifier(), delta);
    }

    @Test
    public void testMageScaling() {
        TestNPC npc = new TestNPC(NpcID.BABOON_MAGE);
        Mage mage = new Mage(npc, 515, 1, 2);
        assertEquals(20, mage.getScaledHealth());
        assertEquals(1.125, mage.getModifier(), delta);
        npc = new TestNPC(NpcID.BABOON_MAGE_11714);
        mage = new Mage(npc, 515, 1, 2);
        assertEquals(1.175, mage.getModifier(), delta);
    }

    @Test
    public void testBrawlerScaling() {
        TestNPC npc = new TestNPC(NpcID.BABOON_BRAWLER);
        Brawler brawler = new Brawler(npc, 515, 1, 2);
        assertEquals(1.125, brawler.getModifier(), delta);
        npc = new TestNPC(NpcID.BABOON_BRAWLER_11712);
        brawler = new Brawler(npc, 515, 1, 2);
        assertEquals(1.175, brawler.getModifier(), delta);
    }

    @Test
    public void testMiscAmpkenScaling() {
        TestNPC shamanNpc = new TestNPC(NpcID.BABOON_SHAMAN);
        Shaman shaman = new Shaman(shamanNpc, 515, 1, 2);
        assertEquals(1.2, shaman.getModifier(), delta);

        TestNPC cursedNPC = new TestNPC(NpcID.CURSED_BABOON);
        Cursed cursed = new Cursed(cursedNPC, 515, 1, 2);
        assertEquals(1.175, cursed.getModifier(), delta);

        TestNPC thrallNPC = new TestNPC(NpcID.BABOON_THRALL);
        Thrall thrall = new Thrall(thrallNPC, 515, 1, 2);
        assertEquals(1.0, thrall.getModifier(), delta);

        TestNPC volatileNPC = new TestNPC(NpcID.VOLATILE_BABOON);
        Volatile vola = new Volatile(volatileNPC, 515, 1, 2);
        assertEquals(1.15, vola.getModifier(), delta);
    }

    @Test
    public void spitterScalingTest() {
        TestNPC spitterNpc = new TestNPC(NpcID.SPITTING_SCARAB);
        Spitter spitter = new Spitter(spitterNpc, 305, 1, 0);
        assertEquals(1.025, spitter.getModifier(), delta);

        TestNPC kephriNpc = new TestNPC(NpcID.KEPHRI);
        Kephri kephri = new Kephri(kephriNpc, 305, 1, 0);
        assertEquals(1.075, kephri.getModifier(), delta);
        assertEquals(330, kephri.getScaledHealth());

        TestNPC kephri721Npc = new TestNPC(NpcID.KEPHRI);
        Kephri721 kephri721 = new Kephri721(kephri721Npc, 305, 1, 0);
        assertEquals(180, kephri721.getScaledHealth());
        assertEquals(1.025, kephri721.getModifier(), delta);
    }

    @Test
    public void akkhaScalingTest() {
        TestNPC akkhaNpc = new TestNPC(NpcID.AKKHA_11790);
        ToaEnemy akkha = new Akkha(akkhaNpc, 305, 2, 0);
        assertEquals(1.575, akkha.getModifier(), delta);
        akkha.fixupStats(305, 2, 0);
        assertEquals(1.575, akkha.getModifier(), delta);
    }

    /**
     * Print the Akkha modifier across invocation 300..500 and party 1..8 for
     * every path level — useful for spotting which configs land on ambiguous
     * scaling bands.
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
                        Akkha akkha = new Akkha(npc, invo, p, path);
                        System.out.printf(" %-7.3f", akkha.getModifier());
                    } catch (Throwable t) {
                        System.out.printf(" %-7s", "err");
                    }
                }
                System.out.println();
            }
        }
    }

    /**
     * Simulate Akkha at a few representative invo/party configs and report
     * predictor accuracy. Confirms the analytical "no ambiguous drops" result
     * empirically.
     */
    @Test
    public void simulateAkkha() {
        int[][] configs = {
                // {invo, party, path}
                {300, 1, 0}, // modifier 1.350 (never-calibrate per gcd, but unambiguous)
                {400, 1, 2}, // modifier ~1.425 (calibratable, unambiguous)
                {500, 1, 6}, // modifier 1.550 (never-calibrate per gcd, but unambiguous)
                {500, 2, 0}, // modifier 1.675 (the party 2+ cap)
        };
        Random rng = new Random(7);
        for (int[] cfg : configs) {
            TestNPC npc = new TestNPC(NpcID.AKKHA_11790);
            Akkha akkha = new Akkha(npc, cfg[0], cfg[1], cfg[2]);
            double scaling = akkha.getModifier();
            Predictor.Properties props =
                    new Predictor.Properties(Skill.HITPOINTS, false, false, scaling);
            Predictor predictor = new Predictor();

            int totalHits = 40;
            int internalFrac = rng.nextInt(10);
            int exact = 0, under = 0, over = 0;
            int firstCalibratedAt = -1;

            for (int i = 0; i < totalHits; i++) {
                int hit = 1 + rng.nextInt(90);
                int precise = Predictor.computePrecise(hit, props);
                int total = precise + internalFrac;
                int xp = total / 10;
                internalFrac = total % 10;
                int predicted = predictor.treePredict(xp, props);
                if (predicted == hit) exact++;
                else if (predicted > hit) over++;
                else under++;
                if (firstCalibratedAt < 0 && predictor.isAccurate(Skill.HITPOINTS)) {
                    firstCalibratedAt = i + 1;
                }
            }

            System.out.printf("Akkha invo=%d p=%d path=%d -> scaling=%.3f | exact=%d under=%d over=%d (calibrated at hit %s)%n",
                    cfg[0], cfg[1], cfg[2], scaling, exact, under, over,
                    firstCalibratedAt < 0 ? "never" : String.valueOf(firstCalibratedAt));
        }
    }
}
