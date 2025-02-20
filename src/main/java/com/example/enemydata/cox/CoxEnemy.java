package com.example.enemydata.cox;

import com.example.enemydata.Enemy;
import com.example.utils.TriFunction;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

import java.util.HashMap;
import java.util.Map;

public class CoxEnemy extends Enemy {
    public static final Map<Integer, TriFunction<NPC, Boolean, Integer, CoxEnemy>> enemies;

    boolean isCm;
    int groupSize;

    static {
        enemies = new HashMap<>();
    }

    protected CoxEnemy(NPC npc, boolean isCm, int groupSize, int baseHealth, int attack, int str, int def, int offAtt, int offStr, int defStab, int defSlash, int defCrush) {
        super(npc, baseHealth, attack, str, def, offAtt, offStr, defStab, defSlash, defCrush);
        this.isCm = isCm;
        this.groupSize = groupSize;
    }
}
