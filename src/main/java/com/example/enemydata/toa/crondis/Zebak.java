package com.example.enemydata.toa.crondis;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Zebak extends ToaEnemy {
    public Zebak(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                580, 250, 140, 70,
                160, 100,
                160, 160, 260);
    }
}
