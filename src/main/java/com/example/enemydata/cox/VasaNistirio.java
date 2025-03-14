package com.example.enemydata.cox;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
public class VasaNistirio extends CoxEnemy {
    public VasaNistirio(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp, 300, 1, 175, 0, 0, 170, 190, 40);
    }

    @Override
    public boolean queueDamage(int damage) {
        shouldDraw = super.queueDamage(damage);
        log.debug("current hp: {} queued damage: {} should draw: {}", current_health, queuedDamage, shouldDraw);
        return shouldDraw;
    }

}
