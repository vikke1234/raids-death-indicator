package com.example.enemydata.toa.ampken;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Cursed extends ToaEnemy {
    public Cursed(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                30, 60, 60, 20,
                25, 0,
                900, 900, 900, true);
    }
}
