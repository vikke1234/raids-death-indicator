package com.example.enemydata.scabaras;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class Kephri721 extends Enemy {
    public Kephri721(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                80, 0, 0, 80,
                0, 0,
                60, 300, 100);
        scaled_health = (int) (Math.round(scaled_health / 10.0) * 10);
        current_health = scaled_health;
    }

    @Override
    public void fixupStats(int invo, int partySize, int pathLevel) {
        super.fixupStats(invo, partySize, pathLevel);
        scaled_health = (int) (Math.round(scaled_health / 10.0) * 10);
        current_health = scaled_health;
    }
}
