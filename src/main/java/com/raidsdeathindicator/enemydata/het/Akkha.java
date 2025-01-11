package com.raidsdeathindicator.enemydata.het;

import com.raidsdeathindicator.enemydata.Enemy;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

@Slf4j
@Getter
@Setter
@Accessors(fluent = true)
public class Akkha  extends Enemy {
    private boolean canPhase;
    private boolean shouldDraw;

    public Akkha(NPC npc, int invocation, int partySize, int pathLevel) {
        super(npc, invocation, partySize, pathLevel,
                400, 100, 140, 80,
                115, 30,
                60, 120, 120);
        canPhase = false;
        // scale to nearest 10, leave scaled health as is, xp modifier is computed using the "real" hp
        stats.current_health = (int) (Math.round(stats.scaled_health / 10.0) * 10);
    }

    @Override
    public boolean queueDamage(int damage) {
        super.queueDamage(damage);
        int queuedDamage = getQueuedDamage();

        final int max_health = (int) (Math.round(stats.scaled_health / 10.0) * 10);
        final int phase_health = max_health / 5;
        // compute what the threshold for the next phase is
        final int next_phase = (stats.current_health / phase_health) * phase_health;
        // This causes her to be highlighted a few hits post shadow, can't fix due to veng being able to overkill too
        shouldDraw = stats.current_health != max_health && (stats.current_health - queuedDamage) <= next_phase;
        log.debug("Akkha: current " + stats.current_health + " queued " + queuedDamage + " next phase: " + next_phase + " draw: " + shouldDraw);
        return shouldDraw;
    }

    @Override
    public void fixupStats(int invo, int partySize, int pathLevel) {
        super.fixupStats(invo, partySize, pathLevel);
        stats.current_health = (int) (Math.round(stats.scaled_health / 10.0) * 10);
    }

    @Override
    public boolean shouldHighlight() {
        return shouldDraw;
    }

    /**
     * Check if the npc id is Akkha. This is required due to the memory special making Akkha disappear and
     * "dies".
     * @param id NPC id
     * @return true if it's Akkha, false otherwise
     */
    public static boolean isAkkha(int id) {
        return id == NpcID.AKKHA_11790 || id == NpcID.AKKHA_11791 || id == NpcID.AKKHA_11792 || id == NpcID.AKKHA_11793;
    }
}
