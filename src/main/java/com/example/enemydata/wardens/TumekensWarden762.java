package com.example.enemydata.wardens;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class TumekensWarden762 extends Enemy {

    public TumekensWarden762(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                880, 150, 150, 150,
                0, 40,
                40, 40, 20);
    }
}
