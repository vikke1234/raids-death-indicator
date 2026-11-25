package com.example.enemydata.toa.wardens;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Obelisk",
        npcIds = { NpcID.TOA_WARDENS_P1_OBELISK_NPC },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class Obelisk extends ToaEnemy {
    public Obelisk(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                260, 200, 150, 100,
                0, 0,
                70, 70, 70);
    }
}
