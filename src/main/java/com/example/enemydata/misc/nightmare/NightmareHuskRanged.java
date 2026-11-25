package com.example.enemydata.misc.nightmare;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

/**
 */
@Tracked(
        name = "Nightmare Husk (Ranged)",
        npcIds = { NpcID.NIGHTMARE_HUSK_RANGED },
        raid = Raid.MISC,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class NightmareHuskRanged extends Enemy {
    public NightmareHuskRanged(NPC npc) {
        super(npc, 20, 1, 1, 20, 0, 0, 20, 20, 20);
    }
}
