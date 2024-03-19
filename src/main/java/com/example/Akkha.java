package com.example;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class Akkha {
    private final Map<Integer, Double> teamScaling;
    private final Map<Integer, Double> pathScaling;

    private final int invocation;
    private final int partySize;
    private final int pathLevel;

    @Setter
    @Getter
    private boolean shouldDraw;

    @Setter
    @Getter
    private boolean canPhase;

    protected int scaled_health;
    protected int current_health;

    // Stats below
    protected int base_health = 400;

    protected int queuedDamage;
    private final int attack = 100;
    private final int str = 140;
    private final int def = 80;
    private final int offAtt = 115;
    private final int offStr = 30;
    private final int defStab = 60;
    private final int defSlash = 120;
    private final int defCrush = 120;


    public Akkha(int invocation, int partySize, int pathLevel) {
        assert(partySize <= 8 && partySize >= 1);
        assert((invocation % 5) == 0);
        this.invocation = invocation;
        this.partySize = partySize;

        this.pathLevel = pathLevel;
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

        scaled_health = getScaledHealth();
        current_health = scaled_health;
        canPhase = true;
    }

    public int getScaledHealth() {
        double scale = (1 + 0.02 * this.invocation / 5);
        int scaled_health = (int) (base_health * scale * teamScaling.get(partySize) * pathScaling.get(pathLevel));

        // scale to nearest 10
        scaled_health = (int) (Math.round(scaled_health / 10.0) * 10);
        return scaled_health;
    }

    private int getAvgLevel() {
        return (attack + str + def + Math.min(getScaledHealth(), 2000)) / 4;
    }

    private int getAvgDef() {
        return (defStab + defSlash + defCrush) / 3;
    }

    /**
     * scales the xp drop according to the
     * <a href="https://oldschool.runescape.wiki/w/Combat#PvM_bonus_experience">wiki</a>
     * @param xpDrop xp drop to scale
     * @return scaled xp drop
     */
    public int scaleXpDrop(int xpDrop) {
        double avgs = Math.floor((getAvgLevel() * (getAvgDef() + offStr + offAtt)) / 5120d);
        avgs /= 40d;
        double scale = 1 + avgs;
        double result = xpDrop / scale;
        return (int) Math.round(result);
    }

    public void hit(int damage) {
        queuedDamage = Math.max(0, queuedDamage - damage);
        current_health -= damage;
    }

    public boolean queueDamage(int damage) {
        queuedDamage += damage;

        final int phase_health = scaled_health / 5;
        // compute what the threshold for the next phase is
        final int next_phase = (current_health / phase_health) * phase_health;
        if (current_health != scaled_health && (current_health - queuedDamage) <= next_phase) {
            shouldDraw = true;
            return true;
        }
        shouldDraw = false;
        return false;
    }
}
