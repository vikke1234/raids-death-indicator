package com.example.enemydata.cox;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
public class SkeletalMystic extends CoxEnemy {
    public SkeletalMystic(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {
        super(npc, isCm, partySize, maxCombat, maxHp, raidTick, 160, 140, 187, 85, 50, 155, 155, 115);
        //regenCycle = 25;
    }
}
