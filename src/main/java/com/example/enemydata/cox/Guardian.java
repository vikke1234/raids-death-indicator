package com.example.enemydata.cox;

import net.runelite.api.NPC;

/**
 * COX Stone Guardians ({@code RAIDS_STONEGUARDIANS_LEFT} / {@code _RIGHT}).
 * Stats per OSRS wiki: hp 250, atk/str 140, def 100, off +0 att / +20 str,
 * defensive bonuses stab 80, slash 180, crush -10.
 */
public class Guardian extends CoxEnemy {
    public Guardian(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp,
                250, 140, 100, 0, 20, 80, 180, -10);
    }

    @Override
    public synchronized boolean queueDamage(int damage) {
        super.queueDamage(damage);
        return false;
    }

    @Override
    public synchronized boolean shouldHighlight() {
        // Guardians are never highlighted (only damageable with a pickaxe and
        // not a kill-priority target — the highlight would be noise).
        return false;
    }
}
