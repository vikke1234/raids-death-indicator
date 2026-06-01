package com.example.enemydata.toa.ampken;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

public class Mage extends ToaEnemy {
    public Mage(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1 ? 20 : 25, // health
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1 ? 40 : 60, // attack
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1 ? 40 : 60, // str
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1 ? 12 : 20, // def
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1 ? 20 : 25, // offatt
                0, 900, 900, 900, true);
    }
}
