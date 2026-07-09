package com.example.enemydata.cox;

import net.runelite.api.NPC;

/**
 * Small (junior) muttadile. Stats: hp 250, atk/str 150, def 138,
 * off +71 att / +48 str, defensive bonuses stab -5, slash +72, crush +50.
 */
public class SmallMuttadile extends Muttadile {
    public SmallMuttadile(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, long raidTick) {
        super(npc, isCm, partySize, maxCombat, maxHp, raidTick,
                250, 150, 138, 71, 48, -5, 72, 50);
    }
}
