package com.example.enemydata.toa.crondis;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Zebak",
        npcIds = { NpcID.TOA_ZEBAK, NpcID.TOA_ZEBAK_ENRAGED },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT,
        boss = true
)
public class Zebak extends ToaEnemy {
    public Zebak(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                580, 250, 140, 70,
                160, 100,
                160, 160, 260);
        hideOnDeath = false;
    }
}
