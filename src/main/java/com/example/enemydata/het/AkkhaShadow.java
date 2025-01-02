package com.example.enemydata.het;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class AkkhaShadow extends Enemy {
    public AkkhaShadow(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                70, 100, 140, 30,
                115, 30,
                60, 120, 120);
            // scale to nearest 10
            stats.scaled_health = (int) (Math.round(stats.scaled_health / 10.0) * 10);
            stats.current_health = stats.scaled_health;
    }

    @Override
    public boolean queueDamage(int damage) {
        return super.queueDamage(damage);
    }
}
