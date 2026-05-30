package com.example.enemydata.cox;

import com.example.enemydata.Enemy;
import com.example.utils.PentFunction;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoxEnemy extends Enemy {
    public static final Map<Integer, PentFunction<NPC, Boolean, Integer, Integer, Integer, CoxEnemy>> enemies;
    public static final Set<Integer> bosses;

    boolean isCm;
    int partySize;
    int maxHp;

    static {
        enemies = new HashMap<>();
        bosses = new HashSet<>();

        enemies.put(NpcID.RAIDS_VESPULA_PORTAL, Portal::new);
        enemies.put(NpcID.RAIDS_SKELETONMYSTIC_A, SkeletalMystic::new);
        enemies.put(NpcID.RAIDS_SKELETONMYSTIC_B, SkeletalMystic::new);
        enemies.put(NpcID.RAIDS_SKELETONMYSTIC_C, SkeletalMystic::new);
        enemies.put(NpcID.RAIDS_LIZARDSHAMAN_A, LizardmanShaman::new);
        enemies.put(NpcID.RAIDS_LIZARDSHAMAN_B, LizardmanShaman::new);
        enemies.put(NpcID.RAIDS_VASANISTIRIO_DORMANT, VasaNistirio::new);
        enemies.put(NpcID.OLM_HAND_LEFT_SPAWNING, OlmMageHand::new);
        enemies.put(NpcID.RAIDS_VANGUARD_DORMANT, Vanguard::new);
        enemies.put(NpcID.RAIDS_VANGUARD_WALKING, Vanguard::new);
        enemies.put(NpcID.RAIDS_VANGUARD_MELEE, Vanguard::new);
        enemies.put(NpcID.RAIDS_VANGUARD_RANGED, Vanguard::new);
        enemies.put(NpcID.RAIDS_VANGUARD_MAGIC, Vanguard::new);

        enemies.put(NpcID.RAIDS_STONEGUARDIANS_LEFT, Guardian::new);
        enemies.put(NpcID.RAIDS_STONEGUARDIANS_RIGHT, Guardian::new);

        enemies.put(NpcID.RAIDS_DOGODILE_SUBMERGED, GreatMuttadile::new);
        enemies.put(NpcID.RAIDS_DOGODILE, GreatMuttadile::new);
        enemies.put(NpcID.RAIDS_DOGODILE_JUNIOR, SmallMuttadile::new);
    }

    protected CoxEnemy(NPC npc, boolean isCm, int partySize, int maxCombat, int maxHp, int baseHealth, int melee, int def, int offAtt, int offStr, int defStab, int defSlash, int defCrush) {
        super(npc, baseHealth, melee, melee, def, offAtt, offStr, defStab, defSlash, defCrush);
        assert (attack == str);
        this.isCm = isCm;
        this.partySize = partySize;
        this.maxHp = maxHp;

        this.scaledHealth = this.getScaledHealth(baseHealth, maxCombat, partySize);
        this.currentHealth = scaledHealth;
        int scaledDef = getScaledDefence(def, partySize, maxHp);
        this.def = scaledDef;
        int scaledMelee = getScaledOffence(melee, partySize, maxHp);
        this.str = scaledMelee;
        this.attack = scaledMelee;
    }

    protected int getScaledDefence(int baseDef, int partySize, int maxHp) {
        return baseDef * (maxHp * 4 / 9 + 55) / 99 * ((int) Math.sqrt(partySize - 1) + (partySize - 1) * 7 / 10 + 100) / 100 * (isCm ? 3 : 2) / 2;
    }

    protected int getScaledOffence(int baseStat, int partySize, int maxHp) {
        return baseStat * (maxHp * 4 / 9 + 55) / 99 * ((int) Math.sqrt(partySize - 1) * 7 + (partySize - 1) + 100) / 100 * (isCm ? 3 : 2) / 2;
    }

    // TODO: override for olm
    protected int getScaledHealth(int baseHp, int maxCombat, int partySize) {
        int combatHpScale = (int) ((maxCombat / 126.0) * baseHp);
        int partyHpScale = (1 + (partySize / 2)) * combatHpScale;

        if (isCm) {
            partyHpScale = partyHpScale * 3 / 2;
        }
        return partyHpScale;
    }
}
