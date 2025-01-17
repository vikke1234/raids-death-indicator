package com.example.enemydata.het;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class AkkhaShadow extends Enemy {
    public AkkhaShadow(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                70, 100, 140, 30,
                115, 30,
                60, 120, 120);
        if (scaled_health > 100)
        {
            final int roundTo = scaled_health > 300 ? 10 : 5;
            // Shadow does not work like akkha, it uses the rounded health in the xp modifier
            scaled_health = ((scaled_health + (roundTo / 2)) / roundTo) * roundTo;
            current_health = scaled_health;
        }
    }

    @Override
    public void fixupStats(int invo, int partySize, int pathLevel) {
        super.fixupStats(invo, partySize, pathLevel);
        if (scaled_health > 100)
        {
            final int roundTo = scaled_health > 300 ? 10 : 5;
            scaled_health = ((scaled_health + (roundTo / 2)) / roundTo) * roundTo;
            current_health = scaled_health;
        }
    }
}
