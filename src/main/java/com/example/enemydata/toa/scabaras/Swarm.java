package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Swarm extends ToaEnemy {
    public Swarm(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                10, 1, 1, 0,
                0, 0,
                -100, -100, -100);
    }
}
