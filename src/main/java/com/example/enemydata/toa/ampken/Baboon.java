package com.example.enemydata.toa.ampken;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Baboon extends ToaEnemy {
    // Baba baboon
    public Baboon(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                35, 0, 0, 50,
                0, 0,
                10, 50, 50, true);
    }
}
