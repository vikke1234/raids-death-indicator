package com.example.enemydata.toa.crondis;

import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Jug extends ToaEnemy {

    public Jug(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                5, 0, 0, 0,
                0, 0,
                0, 0, 0);
    }
}
