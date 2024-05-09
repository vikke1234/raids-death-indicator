package com.example.enemydata;

import net.runelite.api.NPC;

public class Cursed extends Enemy {
    public Cursed(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                10, 60, 60, 20,
                20, 0,
                900, 900, 900);
    }
}