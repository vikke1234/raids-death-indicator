package com.example.enemydata.cox;

import net.runelite.api.NPC;

public class OlmMageHand extends CoxEnemy {
    public OlmMageHand(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp) {
        super(npc, isCm, partySize, maxCombat, maxHp, 600, 250, 175, 0, 0, 200, 200, 200);
    }

    @Override
    protected int getScaledHealth(int baseHp, int maxCombat, int partySize) {
        return 300 * (partySize - partySize / 8 * 3 + 1);
    }

    @Override
    protected int getScaledDefence(int baseDef, int partySize, int maxHp) {
        return baseDef * ((int) Math.sqrt(partySize - 1) + (partySize - 1) * 7 / 10 + 100) / 100 * (isCm ? 3 : 2) / 2;
    }

    @Override
    protected int getScaledOffence(int baseStat, int partySize, int maxHp) {
        return baseStat * ((int) Math.sqrt(partySize - 1) * 7 + (partySize - 1) + 100) / 100 * (isCm ? 3 : 2) / 2;
    }

    @Override
    public boolean queueDamage(int damage) {
        shouldDraw = super.queueDamage(damage);
        //System.out.println("queued damage: " + queuedDamage + " hp: " + current_health);
        return shouldDraw;
    }

    @Override
    public double getModifier() {
        return 1.0d;
    }
}
