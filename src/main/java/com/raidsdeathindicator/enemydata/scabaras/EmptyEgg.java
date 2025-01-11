package com.raidsdeathindicator.enemydata.scabaras;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class EmptyEgg extends Enemy {
    public EmptyEgg(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                35, 0, 0, 80,
                0, 0,
                60, 300, 100);
    }
}
