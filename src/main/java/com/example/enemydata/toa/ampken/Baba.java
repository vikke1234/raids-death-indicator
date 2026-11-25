package com.example.enemydata.toa.ampken;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Baba",
        npcIds = { NpcID.TOA_BABA, NpcID.TOA_BABA_COFFIN, NpcID.TOA_BABA_DIGGING },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE,
        boss = true
)
public class Baba extends ToaEnemy {
    public Baba(NPC npc, int invocation, int partySize, int pathLevel) {
        // TODO revisit this, seems that a new npc is spawned on phase?
        super(npc, invocation, partySize, pathLevel,
                380, 150, 160, 80,
                0, 26,
                80, 160, 240);
    }
}
