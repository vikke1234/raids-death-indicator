package com.example.enemydata.scabaras;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class Arcane extends Enemy {
    public Arcane(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                40, 75, 80, 80,
                0, 55,
                15, 150, 30);
    }
}
