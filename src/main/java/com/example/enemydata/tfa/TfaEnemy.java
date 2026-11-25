package com.example.enemydata.tfa;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

public class TfaEnemy extends Enemy {
    protected TfaEnemy(NPC npc, int baseHealth, int attack, int str, int def,
            int offAtt, int offStr,
            int defStab, int defSlash, int defCrush) {
        super(npc, baseHealth, attack, str, def, offAtt, offStr, defStab, defSlash, defCrush);
        this.hideOnDeath = true;
    }
}
