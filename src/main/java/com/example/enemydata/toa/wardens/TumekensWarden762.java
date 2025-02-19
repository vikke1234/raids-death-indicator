package com.example.enemydata.toa.wardens;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class TumekensWarden762 extends ToaEnemy {

    public TumekensWarden762(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                880, 150, 150, 150,
                0, 40,
                40, 40, 20);
    }
}
