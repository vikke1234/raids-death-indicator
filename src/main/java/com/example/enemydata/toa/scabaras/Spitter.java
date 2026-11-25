package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Spitter Scarab",
        npcIds = { NpcID.TOA_KEPHRI_GUARDIAN_RANGED },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class Spitter extends ToaEnemy {
    public Spitter(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                40, 1, 80, 80,
                0, 55,
                15, 250, 30);
    }

}
