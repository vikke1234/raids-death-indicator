package com.raidsdeathindicator.enemydata.het;

import com.raidsdeathindicator.enemydata.Enemy;
import net.runelite.api.NPC;

public class AkkhaShadow extends Enemy {
    public AkkhaShadow(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                70, 100, 140, 30,
                115, 30,
                60, 120, 120);
        if (stats.scaled_health > 100)
        {
            final int roundTo = stats.scaled_health > 300 ? 10 : 5;
            // Shadow does not work like akkha, it uses the rounded health in the xp modifier
            stats.scaled_health = ((stats.scaled_health + (roundTo / 2)) / roundTo) * roundTo;
            stats.current_health = stats.scaled_health;
        }
    }

    @Override
    public boolean queueDamage(int damage) {
        boolean died = super.queueDamage(damage);
        int queuedDamage = getQueuedDamage();
        System.out.println("Shadow current " + stats.current_health + " queued " + queuedDamage);
        return died;
    }

    @Override
    public void fixupStats(int invo, int partySize, int pathLevel) {
        super.fixupStats(invo, partySize, pathLevel);
        if (stats.scaled_health > 100)
        {
            final int roundTo = stats.scaled_health > 300 ? 10 : 5;
            stats.scaled_health = ((stats.scaled_health + (roundTo / 2)) / roundTo) * roundTo;
            stats.current_health = stats.scaled_health;
        }
    }
}
