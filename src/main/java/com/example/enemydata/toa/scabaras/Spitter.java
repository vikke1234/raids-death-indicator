package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Spitter extends ToaEnemy {
    public Spitter(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                40, 1, 80, 80,
                0, 55,
                15, 250, 30);
    }

}
