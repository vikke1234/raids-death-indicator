package com.example.enemydata.toa.ampken;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

import java.util.HashMap;
import java.util.Map;

public class Boulder extends Enemy {
    // team size -> path level -> hp
    private static final int [][] hpMap = {
                    {25, 25, 27, 27, 31},
                    {36, 36, 39, 39, 45},
                    {47, 47, 51, 51, 58},
                    {55, 55, 60, 60, 68},
                    {62, 62, 68, 68, 77},
                    {70, 70, 77, 77, 87},
                    {77, 77, 84, 84, 96},
                    {85, 85, 93, 93, 106}
            };
    public Boulder(NPC npc, int invocation, int partySize, int pathLevel) {
        // TODO revisit this, seems that a new npc is spawned on phase?
        super(npc, invocation, partySize, pathLevel,
                hpMap[partySize-1][pathLevel], 0, 0, 0,
                0, 0,
                0, 0, 0);
    }
}
