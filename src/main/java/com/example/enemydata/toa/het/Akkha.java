package com.example.enemydata.toa.het;

import com.example.enemydata.toa.ToaEnemy;
import com.example.utils.Trace;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

@Slf4j
@Getter
@Setter
@Accessors(fluent = true)
public class Akkha extends ToaEnemy {
    private boolean canPhase;

    public Akkha(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                400, 100, 140, 80,
                115, 30,
                60, 120, 120);
        canPhase = false;
        // scale to nearest 10, leave scaled health as is, xp modifier is computed using the "real" hp
        currentHealth = (int) (Math.round(scaledHealth / 10.0) * 10);
        hideOnDeath = false;
    }

    @Override
    public synchronized boolean queueDamage(int damage) {
        super.queueDamage(damage);

        final int maxHealth = (int) (Math.round(scaledHealth / 10.0) * 10);
        final int phaseHealth = maxHealth / 5;
        // compute what the threshold for the next phase is
        final int nextPhase = (currentHealth / phaseHealth) * phaseHealth;
        // This causes her to be highlighted a few hits post shadow, can't fix due to veng being able to overkill too
        shouldDraw = currentHealth != nextPhase && (currentHealth - queuedDamage) <= nextPhase;
        Trace.akkha("Akkha: hp={} queued={} nextPhase={} draw={}", currentHealth, queuedDamage, nextPhase, shouldDraw);
        return shouldDraw;
    }

    @Override
    public void fixupStats(int invo, int partySize, int pathLevel) {
        super.fixupStats(invo, partySize, pathLevel);
        currentHealth = (int) (Math.round(scaledHealth / 10.0) * 10);
    }

    /**
     * Check if the npc id is Akkha. This is required due to the memory special making Akkha disappear and
     * "dies".
     * @param id NPC id
     * @return true if it's Akkha, false otherwise
     */
    public static boolean isAkkha(int id) {
        return id == NpcID.AKKHA_MELEE || id == NpcID.AKKHA_RANGE || id == NpcID.AKKHA_MAGE || id == NpcID.AKKHA_ENRAGE_SPAWN;
    }
}
