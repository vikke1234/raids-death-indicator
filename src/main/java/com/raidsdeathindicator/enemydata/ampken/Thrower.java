package com.raidsdeathindicator.enemydata.ampken;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.*;

public class Thrower extends Enemy {
    public Thrower(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                npc.getId() == NpcID.BABOON_THROWER ? 30 : 35, npc.getId() == NpcID.BABOON_THROWER ? 40 : 60,
                npc.getId() == NpcID.BABOON_THROWER ? 40 : 60, npc.getId() == NpcID.BABOON_THROWER ? 12 : 20,
                npc.getId() == NpcID.BABOON_THROWER ? 20 : 25, 0,
                -50, -50, -50, true);
    }
}
