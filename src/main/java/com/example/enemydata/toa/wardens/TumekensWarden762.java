package com.example.enemydata.toa.wardens;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Tumeken's Warden (Phase 3)",
        npcIds = { NpcID.TOA_WARDEN_TUMEKEN_PHASE3 },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class TumekensWarden762 extends ToaEnemy {

    public TumekensWarden762(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                880, 150, 150, 150,
                0, 40,
                40, 40, 20);
    }
}
