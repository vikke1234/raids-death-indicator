package com.example.enemydata.toa.crondis;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Crocodile",
        npcIds = { NpcID.TOA_CRONDIS_CROCODILE },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class Crocodile extends ToaEnemy {
    public Crocodile(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                30, 150, 60, 100,
                0, 100,
                150, 350, 350, true);
    }
}
