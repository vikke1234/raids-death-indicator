package com.example.enemydata.toa;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

import java.util.HashMap;
import java.util.Map;

public class ToaEnemy extends Enemy {
    private static final Map<Integer, Double> teamScaling;
    private static final Map<Integer, Double> pathScaling;

    protected final boolean isPuzzle;
    public int invocation;
    public int partySize;
    public int pathLevel;

    static {
        teamScaling = new HashMap<>();
        teamScaling.put(1, 1.0);
        teamScaling.put(2, 1.9);
        teamScaling.put(3, 2.8);
        teamScaling.put(4, 3.4);
        teamScaling.put(5, 4.0);
        teamScaling.put(6, 4.6);
        teamScaling.put(7, 5.2);
        teamScaling.put(8, 5.8);

        pathScaling = new HashMap<>();
        pathScaling.put(0, 1.0);
        pathScaling.put(1, 1.08);
        pathScaling.put(2, 1.13);
        pathScaling.put(3, 1.18);
        pathScaling.put(4, 1.23);
        pathScaling.put(5, 1.28);
        pathScaling.put(6, 1.33);
    }

    public ToaEnemy(NPC npc, int invocation, int partySize, int pathLevel,
                    int baseHealth, int attack, int str, int def,
                    int offAtt, int offStr,
                    int defStab, int defSlash, int defCrush, boolean isPuzzle) {
        super(npc, baseHealth, attack, str, def, offAtt, offStr, defStab, defSlash, defCrush);
        assert(partySize <= 8 && partySize >= 1);
        assert((invocation % 5) == 0);

        this.isPuzzle = isPuzzle;
        this.shouldDraw = false;
        this.hideOnDeath = true;
        this.queuedDamage = 0;

        if (pathLevel >= 0) {
            this.invocation = invocation;
            this.partySize = partySize;
            this.pathLevel = pathLevel;
            this.scaledHealth = getScaledHealth(invocation, partySize,
                    pathLevel, baseHealth, isPuzzle);
            this.currentHealth = this.scaledHealth;
        }

    }
    public ToaEnemy(NPC npc, int invocation, int partySize, int pathLevel,
                    int baseHealth, int attack, int str, int def,
                    int offAtt, int offStr,
                    int defStab, int defSlash, int defCrush
    ) {
        this(npc, invocation, partySize, pathLevel,
                baseHealth, attack, str, def,
                offAtt, offStr,
                defStab, defSlash, defCrush, false);
    }

    private static int getScaledHealth(int invocation, int partySize, int pathLevel, int baseHealth, boolean isPuzzle) {
        double scale = (1 + 0.004 * invocation);
        double teamScale = teamScaling.get(partySize);
        double pathScale;
        if (isPuzzle) {
            pathScale = 1d;
            teamScale = 1d;
            scale = 1d;
        } else {
            pathScale = pathScaling.get(pathLevel);
        }

        return (int) (baseHealth * scale * teamScale * pathScale);
    }

    public void fixupStats(int invocation, int partySize, int pathLevel) {
        // Should only happen before any npcs actually have been damaged
        this.invocation = invocation;
        this.partySize = partySize;
        this.pathLevel = pathLevel;
        scaledHealth = getScaledHealth(invocation, partySize, pathLevel, baseHealth, isPuzzle);
        currentHealth = scaledHealth;
    }
}
