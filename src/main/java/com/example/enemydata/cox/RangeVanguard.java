package com.example.enemydata.cox;

import net.runelite.api.NPC;

public class RangeVanguard extends CoxEnemy {
    public RangeVanguard(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp, 180, 150, 160, 0, 0, 55, 60, 100);
    }
}
