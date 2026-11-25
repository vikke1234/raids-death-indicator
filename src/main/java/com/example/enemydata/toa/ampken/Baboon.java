package com.example.enemydata.toa.ampken;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Baboon",
        npcIds = { NpcID.TOA_BABA_BABOON },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class Baboon extends ToaEnemy {
    // Baba baboon
    public Baboon(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                35, 0, 0, 50,
                0, 0,
                10, 50, 50, true);
    }
}
