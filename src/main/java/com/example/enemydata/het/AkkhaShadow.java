package com.example.enemydata.het;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class AkkhaShadow extends Enemy {
    public AkkhaShadow(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                70, 100, 140, 30,
                115, 30,
                60, 120, 120);
    }

    @Override
    public boolean queueDamage(int damage) {
        boolean died = super.queueDamage(damage);
        int queuedDamage = getQueuedDamage();
        System.out.println("Shadow current " + stats.current_health + " queued " + queuedDamage);
        return died;
    }
}
