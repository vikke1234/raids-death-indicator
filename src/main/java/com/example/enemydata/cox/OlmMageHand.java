package com.example.enemydata.cox;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import net.runelite.api.NPC;

@Tracked(
        name = "Olm Mage Hand",
        npcIds = { NpcID.OLM_HAND_RIGHT_SPAWNING, NpcID.OLM_HAND_RIGHT },
        raid = Raid.COX,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class OlmMageHand extends CoxEnemy {
    public OlmMageHand(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {
        super(npc, isCm, partySize, maxCombat, maxHp, raidTick, 600, 250, 175, 0, 0, 200, 200, 200);
    }

    @Override
    protected int getScaledHealth(int baseHp, int maxCombat, int partySize) {
        return 300 * (partySize - partySize / 8 * 3 + 1);
    }

    @Override
    protected int getScaledDefence(int baseDef, int partySize, int maxHp) {
        return baseDef * ((int) Math.sqrt(partySize - 1) + (partySize - 1) * 7 / 10 + 100) / 100 * (isCm ? 3 : 2) / 2;
    }

    @Override
    protected int getScaledOffence(int baseStat, int partySize, int maxHp) {
        return baseStat * ((int) Math.sqrt(partySize - 1) * 7 + (partySize - 1) + 100) / 100 * (isCm ? 3 : 2) / 2;
    }

    @Override
    public double getModifier() {
        return 1.0d;
    }
}
