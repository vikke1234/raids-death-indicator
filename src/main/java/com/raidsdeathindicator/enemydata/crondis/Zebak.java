package com.raidsdeathindicator.enemydata.crondis;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class Zebak extends Enemy {
    public Zebak(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                580, 250, 140, 70,
                160, 100,
                160, 160, 260);
    }
}
