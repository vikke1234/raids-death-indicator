package com.example.enemydata.toa.crondis;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Crocodile extends ToaEnemy {
    public Crocodile(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                30, 150, 60, 100,
                0, 100,
                150, 350, 350, true);
    }
}
