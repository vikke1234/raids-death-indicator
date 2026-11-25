package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Kephri (Enrage)",
        npcIds = { NpcID.TOA_KEPHRI_BOSS_ENRAGE },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT_AND_HIDE,
        boss = true
)
public class Kephri721 extends ToaEnemy {
    public Kephri721(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                80, 0, 0, 80,
                0, 0,
                60, 300, 100);
        scaledHealth = (int) (Math.round(scaledHealth / 10.0) * 10);
        currentHealth = scaledHealth;
    }

    @Override
    public void fixupStats(int invo, int partySize, int pathLevel) {
        super.fixupStats(invo, partySize, pathLevel);
        scaledHealth = (int) (Math.round(scaledHealth / 10.0) * 10);
        currentHealth = scaledHealth;
    }
}
