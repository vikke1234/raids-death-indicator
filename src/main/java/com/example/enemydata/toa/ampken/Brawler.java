package com.example.enemydata.toa.ampken;

import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

public class Brawler extends ToaEnemy {
    public Brawler(NPC npc, int invocation, int partySize, int pathLevel) {
        // For solo 380, this should likely be have a scaling of 1.12?
        super(npc, invocation, partySize, pathLevel,
                npc.getId() == NpcID.BABOON_BRAWLER ? 25 : 30, // health
                npc.getId() == NpcID.BABOON_BRAWLER ? 40 : 60, // att
                npc.getId() == NpcID.BABOON_BRAWLER ? 40 : 60, // str
                npc.getId() == NpcID.BABOON_BRAWLER ? 12 : 20, // def
                npc.getId() == NpcID.BABOON_BRAWLER ? 20 : 25, // offatt
                0, 900, 900, 900, true);
    }
}
