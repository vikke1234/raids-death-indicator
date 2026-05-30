package com.example.enemydata.cox;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

/**
 * Vanguards cycle through combat styles on the same NPC instance via
 * {@link net.runelite.api.events.NpcChanged}: id 7527 = melee, 7528 = ranged,
 * 7529 = magic, 7525/7526 = sleep/rest. Their base stats are identical across
 * styles; only offensive bonuses differ (melee gets +20 attack / +10 strength,
 * ranged and magic both get 0). {@link #getModifier()} is therefore evaluated
 * dynamically from the live NPC id rather than baked in at construction.
 */
@Slf4j
public class Vanguard extends CoxEnemy {
    public Vanguard(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        // hp=180, melee=150 (atk and str both 150), def=160, off bonuses
        // overridden per-style in getModifier(), defStab/Slash/Crush=160.
        super(npc, isCm, partySize, maxCombat, maxHp,
                180, 150, 160, 0, 0, 160, 160, 160);
    }

    @Override
    public synchronized boolean queueDamage(int damage) {
        shouldDraw = super.queueDamage(damage);
        log.debug("vanguard hp: {} queued: {} draw: {}", currentHealth, queuedDamage, shouldDraw);
        return shouldDraw;
    }

    @Override
    public synchronized int heal(int amount) {
        // Vanguards group-heal back to full regardless of the hitsplat value.
        // The displayed heal amount only reflects the gap to the group's
        // pre-heal max, so use scaledHealth directly as the truth. Also clear
        // queued damage since the heal mechanic absorbs in-flight hits.
        currentHealth = scaledHealth;
        queuedDamage = 0;
        shouldDraw = false;
        return currentHealth;
    }

    @Override
    public double getModifier() {
        NPC npc = getNpc();
        int id = npc != null ? npc.getId() : -1;
        // Per-style stab/slash/crush + offensive bonuses (OSRS wiki values).
        // Magic Vanguard's modifier comes out ~6% short of empirical because
        // magic attacks should use defMagic, not the stab/slash/crush average.
        // defMagic isn't tracked yet.
        int avgDef;
        int offS;
        int offA;
        switch (id) {
            case NpcID.RAIDS_VANGUARD_MELEE:
                avgDef = (150 + 150 + 150) / 3;
                offS = 10;
                offA = 20;
                break;
            case NpcID.RAIDS_VANGUARD_RANGED:
                avgDef = (55 + 60 + 100) / 3;
                offS = 0;
                offA = 0;
                break;
            case NpcID.RAIDS_VANGUARD_MAGIC:
                avgDef = (315 + 340 + 400) / 3;
                offS = 0;
                offA = 0;
                break;
            default:
                // Dormant / walking states — no active combat style yet, use
                // the melee-vang defence as a neutral.
                avgDef = (150 + 150 + 150) / 3;
                offS = 0;
                offA = 0;
                break;
        }
        return computeModifier(avgDef, offS, offA);
    }
}
