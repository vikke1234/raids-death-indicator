package com.example.enemydata;

import net.runelite.api.NPC;

public class Kephri extends Enemy {
    // TODO: might need extra handling due to shield? Probably just to ignore the damage queueing
    public Kephri(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                80, 0, 0, 80,
                0, 0,
                60, 300, 100);
    }
}
