package com.example.enemydata.cox;

import net.runelite.api.NPC;

public class MeleeVanguard extends CoxEnemy {
    public MeleeVanguard(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp, 180, 150, 160, 20, 10, 150, 150, 150);
    }
}
