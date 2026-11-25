package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Agile Scarab",
        npcIds = { NpcID.TOA_KEPHRI_SCARAB_RANGEKITE },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class AgileScarab extends ToaEnemy {
    public AgileScarab(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                30, 60, 20, 5,
                0, 25,
                0, 0, 0, true);
    }
}
