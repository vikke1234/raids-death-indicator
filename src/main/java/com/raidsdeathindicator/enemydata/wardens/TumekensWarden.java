package com.raidsdeathindicator.enemydata.wardens;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class TumekensWarden extends Enemy {
    public TumekensWarden(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                140, 300, 150, 100,
                0, 25,
                70, 70, 70);
    }
}
