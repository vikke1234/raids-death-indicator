package com.example;

import com.example.enemydata.cox.SkeletalMystic;
import net.runelite.api.NpcID;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoxScalingTests {
    private static final float delta = 0.00000001F;

    @Test
    public void testMysticScaling() {
        TestNPC npc = new TestNPC(NpcID.SKELETAL_MYSTIC);
        SkeletalMystic mystic = new SkeletalMystic(npc, true, 3, 126, 99);
        assertEquals(480, mystic.scaled_health);
    }
}
