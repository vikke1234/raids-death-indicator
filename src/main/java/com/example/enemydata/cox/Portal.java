package com.example.enemydata.cox;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
public class Portal extends CoxEnemy {
    public Portal(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp, 250, 1, 176, 0, 0, 0, 0, 0);
    }

    @Override
    public boolean queueDamage(int damage) {
        shouldDraw = super.queueDamage(damage);
        log.debug("current hp: {} queued damage: {} should draw: {}", current_health, queuedDamage, shouldDraw);
        return shouldDraw;
    }

    @Override
    public double getModifier() {
        return 1.0d;
    }
}
