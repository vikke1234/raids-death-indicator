package com.raidsdeathindicator.enemydata.wardens;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class Obelisk extends Enemy {
    public Obelisk(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                260, 200, 150, 100,
                0, 0,
                70, 70, 70);
    }
}
