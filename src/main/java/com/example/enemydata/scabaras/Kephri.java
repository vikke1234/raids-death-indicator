package com.example.enemydata.scabaras;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class Kephri extends Enemy {
    // TODO: might need extra handling due to shield? Probably just to ignore the damage queueing
    public Kephri(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                150, 0, 0, 80,
                0, 0,
                60, 300, 100);
    }

    /**
     * Disable for kephri, no use for it either way. If requested, can try to fix.
     * @param damage damage dealt
     * @return false
     */
    @Override
    public boolean queueDamage(int damage) {
        return false;
    }
}
