package com.example.enemydata.toa.ampken;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class Shaman extends Enemy {
    public Shaman(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                16, 60, 60, 20,
                25, 0,
                900, 900, 900, true);
    }
}
