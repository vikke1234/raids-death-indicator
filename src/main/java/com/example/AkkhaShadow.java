package com.example;

import lombok.Getter;
import net.runelite.api.NPC;

public class AkkhaShadow extends Akkha {
    @Getter
    private final NPC npc;

    public AkkhaShadow(int invocation, int partySize, int pathLevel, NPC npc) {
        super(invocation, partySize, pathLevel, 70);
        this.npc = npc;
    }

    @Override
    public void hit(int damage) {
        super.hit(damage);
    }

    @Override
    public boolean queueDamage(int damage) {
        this.queuedDamage += damage;
        boolean shouldDie = current_health - queuedDamage == 0;
        System.out.println("Shadow " + current_health + " queued " + queuedDamage);
        return shouldDie;
    }
}
