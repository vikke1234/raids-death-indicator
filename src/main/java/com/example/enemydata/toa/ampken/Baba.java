package com.example.enemydata.toa.ampken;

import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Baba extends ToaEnemy {
    public Baba(NPC npc, int invocation, int partySize, int pathLevel) {
        // TODO revisit this, seems that a new npc is spawned on phase?
        super(npc, invocation, partySize, pathLevel,
                380, 150, 160, 80,
                0, 26,
                80, 160, 240);
    }
}
