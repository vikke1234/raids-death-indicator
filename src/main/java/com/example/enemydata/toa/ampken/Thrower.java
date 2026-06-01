package com.example.enemydata.toa.ampken;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

public class Thrower extends ToaEnemy {
    public Thrower(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1 ? 30 : 35, npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1 ? 40 : 60,
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1 ? 40 : 60, npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1 ? 12 : 20,
                npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1 ? 20 : 25, 0,
                -50, -50, -50, true);
    }
}
