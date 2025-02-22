package com.example.enemydata.cox;

import net.runelite.api.NPC;

public class Tekton extends CoxEnemy {
    public Tekton(NPC npc, boolean isCm, int groupSize, int maxCombat, int maxHp) {
        super(npc, isCm, groupSize, maxCombat, maxHp, 300, 390, 205, 64, 20, 155, 165, 105);
        hideOnDeath = false;
        System.out.println("tek hp: " + current_health);
        System.out.println("slash def: " + this.defSlash + " stab def: " + this.defStab + " crush def: " + this.defCrush);
    }

    @Override
    protected int getScaledDefence(int baseDef, int partySize, int maxHp) {
        return 205 * (maxHp * 4 / 9 + 55) / 99 * ((int) Math.sqrt(partySize - 1) + (partySize - 1) * 7 / 10 + 100) / 100 * (isCm ? 6 : 5) / 5;
    }

    public void swapForm(boolean enrage) {
        if (enrage) {
            this.defSlash = 280;
            this.defStab = 290;
            this.defCrush = 180;
            this.offStr = 30;
        } else {
            this.defSlash = 155;
            this.defStab = 165;
            this.defCrush = 105;
            this.offStr = 20;
        }
    }
}