package com.example.enemydata.toa.ampken;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Volatile Baboon",
        npcIds = { NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class Volatile extends ToaEnemy {
    public Volatile(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                8, 60, 60, 20,
                25, 0,
                900, 900, 900, true);
    }
}
