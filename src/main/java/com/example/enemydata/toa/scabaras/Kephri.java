package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

@Tracked(
        name = "Kephri",
        npcIds = { NpcID.TOA_KEPHRI_BOSS_SHIELDED },
        raid = Raid.TOA,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class Kephri extends ToaEnemy {
    // TODO: might need extra handling due to shield? Probably just to ignore the damage queueing
    public Kephri(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                150, 0, 0, 80,
                0, 0,
                60, 300, 100);
        scaledHealth = (int) (Math.round(scaledHealth / 10.0) * 10);
        currentHealth = scaledHealth;
        hideOnDeath = false;
    }

    @Override
    public void fixupStats(int invo, int partySize, int pathLevel) {
        super.fixupStats(invo, partySize, pathLevel);
        scaledHealth = (int) (Math.round(scaledHealth / 10.0) * 10);
        currentHealth = scaledHealth;
    }

    /**
     * Disable for kephri, no use for it either way. If requested, can try to fix.
     * @param damage damage dealt
     * @return false
     */
    @Override
    public synchronized boolean queueDamage(int damage) {
        return false;
    }
}
