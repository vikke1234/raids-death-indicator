package com.example.enemydata.cox;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
public class Portal extends CoxEnemy {
    public Portal(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {
        super(npc, isCm, partySize, maxCombat, maxHp, raidTick, 250, 1, 176, 0, 0, 0, 0, 0);
        regenCycle = 45;
    }


    @Override
    public double getModifier() {
        return 1.0d;
    }
}
