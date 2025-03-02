package com.example.enemydata.cox;

import net.runelite.api.NPC;
import net.runelite.api.NpcID;

public class Vanguard extends CoxEnemy {
    public Vanguard(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public CoxEnemy correctVanguard(int id) {
        switch (id) {
            case NpcID.VANGUARD_7527:
                return new MeleeVanguard(npc, isCm, partySize, maxCombat, maxHp);
            case NpcID.VANGUARD_7528:
                return new RangeVanguard(npc, isCm, partySize, maxCombat, maxHp);
            case NpcID.VANGUARD_7529:
                return new MageVanguard(npc, isCm, partySize, maxCombat, maxHp);
        }
        return null;
    }
}
