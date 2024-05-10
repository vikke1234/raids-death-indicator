package com.example.enemydata;

import net.runelite.api.NPC;
import net.runelite.api.NpcID;

public class Thrower extends Enemy {
    public Thrower(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                npc.getId() == NpcID.BABOON_THROWER ? 4 : 6, 40, 40, 12,
                20, 0,
                -50, -50, -50, true);
    }
}
