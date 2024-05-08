package com.example.enemydata;

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
        return super.queueDamage(damage);
    }
}
