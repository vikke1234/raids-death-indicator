package com.example.enemydata.crondis;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class Crocodile extends Enemy {
    public Crocodile(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                30, 150, 60, 100,
                0, 100,
                150, 350, 350);
    }
}
