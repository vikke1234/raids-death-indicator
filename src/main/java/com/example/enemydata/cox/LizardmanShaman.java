package com.example.enemydata.cox;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
public class LizardmanShaman extends CoxEnemy {
    public LizardmanShaman(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp, 190, 130, 210, 58, 52, 102, 160, 150);
    }

    @Override
    public boolean queueDamage(int damage) {
        shouldDraw = super.queueDamage(damage);
        log.debug("current hp: {} queued damage: {} should draw: {}", current_health, queuedDamage, shouldDraw);
        return shouldDraw;
    }

}
