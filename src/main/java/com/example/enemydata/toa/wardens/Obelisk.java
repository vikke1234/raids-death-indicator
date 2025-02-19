package com.example.enemydata.toa.wardens;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ToaEnemy;
import net.runelite.api.NPC;

public class Obelisk extends ToaEnemy {
    public Obelisk(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                260, 200, 150, 100,
                0, 0,
                70, 70, 70);
    }
}
