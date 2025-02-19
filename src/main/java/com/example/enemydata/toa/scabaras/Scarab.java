package com.example.enemydata.toa.scabaras;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Scarab extends ToaEnemy {
    public Scarab(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                12, 20, 32, 28,
                0, 0,
                0, 0, 0);
    }

}
