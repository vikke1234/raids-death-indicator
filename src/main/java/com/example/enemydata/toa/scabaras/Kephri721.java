package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

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
