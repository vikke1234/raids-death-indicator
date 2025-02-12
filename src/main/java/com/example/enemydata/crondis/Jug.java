package com.example.enemydata.crondis;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class Jug extends Enemy {

    public Jug(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                5, 0, 0, 0,
                0, 0,
                0, 0, 0, true);
        instakill = true;
    }
}
