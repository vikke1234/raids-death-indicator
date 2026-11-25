package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Scarab",
        npcIds = { NpcID.TOA_SCABARAS_SCARAB },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class Scarab extends ToaEnemy {
    public Scarab(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                12, 20, 32, 28,
                0, 0,
                0, 0, 0);
    }

}
