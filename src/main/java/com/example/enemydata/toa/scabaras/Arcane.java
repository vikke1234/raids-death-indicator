package com.example.enemydata.toa.scabaras;

import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Arcane extends ToaEnemy {
    public Arcane(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                40, 75, 80, 80,
                0, 55,
                15, 150, 30);
    }
}
