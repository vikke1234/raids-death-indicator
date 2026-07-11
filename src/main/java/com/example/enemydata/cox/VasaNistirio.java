package com.example.enemydata.cox;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
public class VasaNistirio extends CoxEnemy {
    public VasaNistirio(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {
        super(npc, isCm, partySize, maxCombat, maxHp, raidTick, 300, 1, 175, 0, 0, 170, 190, 50);
        regenCycle = 10;
    }


}
