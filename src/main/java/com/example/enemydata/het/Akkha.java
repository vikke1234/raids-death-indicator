package com.example.enemydata.het;

import com.example.enemydata.Enemy;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runelite.api.NPC;

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
        // scale to nearest 10
        stats.scaled_health = (int) (Math.round(stats.scaled_health / 10.0) * 10);
    }

    @Override
    public boolean queueDamage(int damage) {
        super.queueDamage(damage);
        int queuedDamage = getQueuedDamage();

        final int phase_health = stats.scaled_health / 5;
        // compute what the threshold for the next phase is
        final int next_phase = (stats.current_health / phase_health) * phase_health;
        //System.out.println("Akkha: current " + stats.current_health + " queued " + queuedDamage);
        shouldDraw = stats.current_health != next_phase && (stats.current_health - queuedDamage) <= next_phase;
        return shouldDraw;
    }

    @Override
    public boolean shouldHighlight() {
        return shouldDraw;
    }

    @Override
    public void nearbyDied(NPC npc) {
    }
}
