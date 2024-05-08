package com.example.enemydata;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Builder(builderMethodName = "hiddenBuilder")
public class NPCStats {

    public int queuedDamage;

    public int invocation;
    public int partySize;
    public int pathLevel;

    public final int base_health;
    public int scaled_health;
    public int current_health;

    private final int attack;
    private final int str;
    private final int def;
    private final int offAtt;
    private final int offStr;
    private final int defStab;
    private final int defSlash;
    private final int defCrush;

    private static final Map<Integer, Double> teamScaling;
    private static final Map<Integer, Double> pathScaling;

    static {
        // Ew
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

    public static NPCStatsBuilder builder(int invocation, int partySize, int pathLevel, int baseHealth) {
        int scaled = getScaledHealth(invocation, partySize, pathLevel, baseHealth);
        return hiddenBuilder().base_health(baseHealth).scaled_health(scaled).current_health(scaled).invocation(invocation).partySize(partySize).pathLevel(pathLevel);
    }
    public static NPCStatsBuilder builder(int baseHealth) {
        return hiddenBuilder().base_health(baseHealth);
    }

    public void fixup(int invocation, int partySize, int pathLevel) {
        this.invocation = invocation;
        this.partySize = partySize;
        this.pathLevel = pathLevel;
        scaled_health = getScaledHealth(invocation, partySize, pathLevel, base_health);
        current_health = scaled_health;
    }
    public double getModifier() {
        double avgs = Math.floor((getAvgLevel() * (getAvgDef() + offStr + offAtt)) / 5120d);
        avgs /= 40d;
        double scale = 1 + avgs;
        return scale;
    }

    public int getScaledHealth() {
        double scale = (1 + 0.02 * this.invocation / 5);
        int scaled_health = (int) (base_health * scale * teamScaling.get(partySize) * pathScaling.get(pathLevel));

        // scale to nearest 10
        scaled_health = (int) (Math.round(scaled_health / 10.0) * 10);
        return scaled_health;
    }

    private static int getScaledHealth(int invocation, int partySize, int pathLevel, int base_health) {
        double scale = (1 + 0.02 * invocation / 5);
        double teamScale = teamScaling.get(partySize);
        double pathScale = pathScaling.get(pathLevel);
        int scaled_health = (int) (base_health * scale * teamScale * pathScale);

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

    public void hit(int damage) {
        queuedDamage = Math.max(0, queuedDamage - damage);
        current_health -= damage;
    }

    public boolean queueDamage(int damage) {
        queuedDamage += damage;
        return queuedDamage >= current_health;
    }
}
