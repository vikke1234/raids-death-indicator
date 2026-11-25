package com.example.enemydata.toa.ampken;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Thrall",
        npcIds = { NpcID.TOA_PATH_APMEKEN_BABOON_THRALL },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE
)
public class Thrall extends ToaEnemy {
    public Thrall(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                2, 40, 40, 12,
                20, 0,
                0, 0, 0, false); // HP does indeed seem to scale by invocation?
    }
}
