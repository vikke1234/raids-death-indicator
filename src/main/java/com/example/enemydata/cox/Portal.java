package com.example.enemydata.cox;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;

@Slf4j
@Tracked(
        name = "Vespera Portal",
        npcIds = { NpcID.RAIDS_VESPULA_PORTAL },
        raid = Raid.COX,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class Portal extends CoxEnemy {
    public Portal(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {
        super(npc, isCm, partySize, maxCombat, maxHp, raidTick, 250, 1, 176, 0, 0, 0, 0, 0);
        //regenCycle = 45;
    }


    @Override
    public double getModifier() {
        return 1.0d;
    }
}
