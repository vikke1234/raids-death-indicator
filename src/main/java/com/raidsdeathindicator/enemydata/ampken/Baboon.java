package com.raidsdeathindicator.enemydata.ampken;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class Baboon extends Enemy {
    // Baba baboon
    public Baboon(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                35, 0, 0, 50,
                0, 0,
                10, 50, 50);
    }
}
