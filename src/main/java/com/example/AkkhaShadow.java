package com.example;

import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class AkkhaShadow extends Akkha {
    @Getter
    private final NPC npc;

    public AkkhaShadow(int invocation, int partySize, int pathLevel, NPC npc, ModelOutlineRenderer renderer) {
        super(invocation, partySize, pathLevel);
        this.base_health = 70;
        this.scaled_health = getScaledHealth();
        this.npc = npc;
    }

    @Override
    public boolean queueDamage(int damage) {
        this.queuedDamage += damage;
        System.out.println("current " + current_health + " queued damage: " + queuedDamage);
        if (current_health - queuedDamage == 0) {
            return true;
        }
        return false;
    }
}
