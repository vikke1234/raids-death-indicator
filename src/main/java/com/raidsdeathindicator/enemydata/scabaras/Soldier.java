package com.raidsdeathindicator.enemydata.scabaras;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class Soldier extends Enemy {
    public Soldier(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                40, 75, 80, 80,
                0, 55,
                15, 250, 30);
    }
}
