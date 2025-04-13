package com.example;

import com.example.TestNPC;
import com.example.enemydata.toa.ToaEnemy;
import com.example.enemydata.toa.ampken.*;
import com.example.enemydata.toa.het.Akkha;
import com.example.enemydata.toa.scabaras.Kephri;
import com.example.enemydata.toa.scabaras.Kephri721;
import com.example.enemydata.toa.scabaras.Spitter;
import net.runelite.api.*;
import org.junit.Test;

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
        assertEquals(12, mage.getScaledHealth());
        assertEquals(1.1, mage.getModifier(), delta);
        npc = new TestNPC(NpcID.BABOON_MAGE_11714);
        mage = new Mage(npc, 515, 1, 2);
        assertEquals(1.175, mage.getModifier(), delta);
    }

    @Test
    public void testBrawlerScaling() {
        TestNPC npc = new TestNPC(NpcID.BABOON_BRAWLER);
        Brawler brawler = new Brawler(npc, 515, 1, 2);
        assertEquals(1.1, brawler.getModifier(), delta);
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
        assertEquals(1.175, vola.getModifier(), delta);
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
}
