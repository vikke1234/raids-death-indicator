package com.example.enemydata.cox;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
@Tracked(
        name = "Skeletal Mystic",
        npcIds = { NpcID.RAIDS_SKELETONMYSTIC_A, NpcID.RAIDS_SKELETONMYSTIC_B, NpcID.RAIDS_SKELETONMYSTIC_C },
        raid = Raid.COX,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class SkeletalMystic extends CoxEnemy {
    public SkeletalMystic(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {
        super(npc, isCm, partySize, maxCombat, maxHp, raidTick, 160, 140, 187, 85, 50, 155, 155, 115);
        //regenCycle = 25;
    }
}
