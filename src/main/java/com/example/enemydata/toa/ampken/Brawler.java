package com.example.enemydata.toa.ampken;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

public class Brawler extends ToaEnemy {
    public Brawler(NPC npc, int invocation, int partySize, int pathLevel) {
        // For solo 380, this should likely be have a scaling of 1.12?
        super(npc, invocation, partySize, pathLevel,
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1 ? 25 : 30, // health
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1 ? 40 : 60, // att
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1 ? 40 : 60, // str
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1 ? 12 : 20, // def
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1 ? 20 : 25, // offatt
                0, 900, 900, 900, true);
    }
}
