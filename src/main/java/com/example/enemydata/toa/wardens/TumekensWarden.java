package com.example.enemydata.toa.wardens;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class TumekensWarden extends ToaEnemy {
    public TumekensWarden(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                140, 300, 150, 100,
                0, 25,
                70, 70, 70);
    }
}
