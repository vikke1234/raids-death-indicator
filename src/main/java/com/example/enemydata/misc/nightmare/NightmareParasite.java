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
        name = "Nightmare Parasite",
        npcIds = { NpcID.NIGHTMARE_PARASITE_WEAK, NpcID.NIGHTMARE_PARASITE },
        raid = Raid.MISC,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class NightmareParasite extends Enemy {
    public NightmareParasite(NPC npc) {
        super(npc, 40, 1, 1, 40, 0, 0, 50, 70, 10);
    }
}
