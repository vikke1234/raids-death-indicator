package com.example.enemydata.tfa;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TfaEnemy extends Enemy {
    public static final Map<Integer, Function<NPC, Enemy>> enemies;

    static {
        enemies = new HashMap<>();
        enemies.put(NpcID.TFA_GUARDIAN_START, ArchiveGuardian::new);
        enemies.put(NpcID.TFA_GUARDIAN, ArchiveGuardian::new);
        enemies.put(NpcID.TFA_GUARDIAN_UNSHIELDED, ArchiveGuardian::new);
    }

    protected TfaEnemy(NPC npc, int baseHealth, int attack, int str, int def,
            int offAtt, int offStr,
            int defStab, int defSlash, int defCrush) {
        super(npc, baseHealth, attack, str, def, offAtt, offStr, defStab, defSlash, defCrush);
        this.hideOnDeath = true;
    }
}
