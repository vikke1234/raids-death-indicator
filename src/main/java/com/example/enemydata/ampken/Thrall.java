package com.example.enemydata.ampken;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class Thrall extends Enemy {
    public Thrall(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                2, 40, 40, 12,
                20, 0,
                0, 0, 0, true);
    }
}
