package com.example.enemydata;

import net.runelite.api.NPC;
import net.runelite.api.NpcID;

public class Brawler extends Enemy {
    public Brawler(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                npc.getId() == NpcID.BABOON_BRAWLER ? 4 : 6, 40, 40, 12,
                20, 0,
                900, 900, 900, true);
    }
}
