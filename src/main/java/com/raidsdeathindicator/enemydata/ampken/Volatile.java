package com.raidsdeathindicator.enemydata.ampken;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class Volatile extends Enemy {
    public Volatile(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                8, 60, 60, 20,
                25, 0,
                900, 900, 900, true);
    }
}
