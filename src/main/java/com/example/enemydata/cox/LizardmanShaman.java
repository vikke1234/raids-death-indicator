package com.example.enemydata.cox;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
public class LizardmanShaman extends CoxEnemy {
    public LizardmanShaman(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {
        super(npc, isCm, partySize, maxCombat, maxHp, raidTick, 190, 130, 210, 58, 52, 102, 160, 150);
        //regenCycle = 20;
    }


}
