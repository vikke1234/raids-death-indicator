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
        name = "Nightmare Husk (Mage)",
        npcIds = { NpcID.NIGHTMARE_HUSK_MAGIC },
        raid = Raid.MISC,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class NightmareHuskMage extends Enemy {
    public NightmareHuskMage(NPC npc) {
        super(npc, 20, 1, 1, 20, 0, 0, 20, 20, 20);
    }
}
