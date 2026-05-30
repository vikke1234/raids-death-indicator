package com.example.enemydata.cox;

import net.runelite.api.NPC;

/**
 * Great (mother) muttadile, in both its submerged and surfaced forms. Stats:
 * hp 250, atk/str 250, def 220, off +88 att / +55 str, defensive bonuses
 * stab -5, slash +82, crush +60. Heal-at-meat-tree mechanic isn't modeled
 * beyond the inherited 50% highlight flag.
 */
public class GreatMuttadile extends Muttadile {
    public GreatMuttadile(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp,
                250, 250, 220, 88, 55, -5, 82, 60);
    }
}
