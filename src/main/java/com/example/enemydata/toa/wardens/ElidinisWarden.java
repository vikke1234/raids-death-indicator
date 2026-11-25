package com.example.enemydata.toa.wardens;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Elidini's Warden",
        npcIds = { NpcID.TOA_WARDEN_ELIDINIS_PHASE2_MAGE, NpcID.TOA_WARDEN_ELIDINIS_PHASE2_RANGE },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE,
        boss = true
)
public class ElidinisWarden extends ToaEnemy {
    public ElidinisWarden(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                140, 300, 150, 100,
                0, 10,
                70, 70, 70);
    }
}
