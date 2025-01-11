package com.raidsdeathindicator.enemydata.scabaras;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class Scarab extends Enemy {
    public Scarab(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                12, 20, 32, 28,
                0, 0,
                0, 0, 0);
    }

}
