package com.example.enemydata.cox;

import net.runelite.api.NPC;

/**
 * Shared behavior for great + small muttadiles: a "below 50% hp" highlight
 * raised when predicted (queued) damage will take the boss below half the
 * scaled max, and cleared on the actual hitsplat that brings real HP below
 * the threshold.
 */
public abstract class Muttadile extends CoxEnemy {
    private boolean halfHpFlagged = false;

    protected Muttadile(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp,
                        int baseHp, int melee, int def,
                        int offAtt, int offStr,
                        int defStab, int defSlash, int defCrush) {
        super(npc, isCm, partySize, maxCombat, maxHp,
                baseHp, melee, def, offAtt, offStr, defStab, defSlash, defCrush);
    }

    @Override
    public synchronized boolean queueDamage(int damage) {
        int hpBefore = currentHealth - queuedDamage;
        boolean willDie = super.queueDamage(damage);
        int hpAfter = currentHealth - queuedDamage;

        if (willDie) {
            shouldDraw = true;
        } else if (hpBefore * 2 > scaledHealth && hpAfter * 2 <= scaledHealth) {
            halfHpFlagged = true;
            shouldDraw = true;
        }
        return shouldDraw;
    }

    @Override
    public synchronized int hit(int damage) {
        int hp = super.hit(damage);
        if (halfHpFlagged && hp * 2 <= scaledHealth) {
            halfHpFlagged = false;
            shouldDraw = false;
        }
        return hp;
    }
}
