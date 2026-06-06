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
        enemies.put(NpcID.OLM_HAND_RIGHT_SPAWNING, OlmMageHand::new);
        enemies.put(NpcID.OLM_HAND_RIGHT, OlmMageHand::new);
        enemies.put(NpcID.RAIDS_VANGUARD_DORMANT, Vanguard::new);
        enemies.put(NpcID.RAIDS_VANGUARD_WALKING, Vanguard::new);
        enemies.put(NpcID.RAIDS_VANGUARD_MELEE, Vanguard::new);
        enemies.put(NpcID.RAIDS_VANGUARD_RANGED, Vanguard::new);
        enemies.put(NpcID.RAIDS_VANGUARD_MAGIC, Vanguard::new);

        // Disabled for now, until a way to calculate the average mining level
        // for a raid is figured out. Guardians will also likely never work
        // properly unless everyone is in party. That or some other cursed
        // shenanigans.
        //enemies.put(NpcID.RAIDS_STONEGUARDIANS_LEFT, Guardian::new);
        //enemies.put(NpcID.RAIDS_STONEGUARDIANS_RIGHT, Guardian::new);

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
        System.out.println("scaled health " + this.scaledHealth + " party size " + partySize);
        this.currentHealth = scaledHealth;
        int scaledDef = getScaledDefence(def, partySize, maxHp);
        this.def = scaledDef;
        int scaledMelee = getScaledOffence(melee, partySize, maxHp);
        this.str = scaledMelee;
        this.attack = scaledMelee;
    }

    // CoX scaling — ported from the OSRS DPS calculator's applyMultiCoxScaling.
    // https://github.com/weirdgloop/osrs-dps-calc / src/lib/scaling/CoxMonsterScaling.ts
    //
    // Inputs clamp:
    //   highestComLevel ∈ [60, 126]
    //   highestHp       ∈ [55, 99]   (curve: 55 + 44·maxHp/99)
    //   partySize       ∈ [1, 100]
    //
    // Outputs clamp (sanity from the calculator):
    //   hp        ∈ [50,   30_000]
    //   offensive ∈ [50,    5_000]
    //   defensive ∈ [50,   20_000]
    //
    // CM is a flat +50% on everything we currently track (Tekton + glowing
    // crystal special cases from the calculator are not modelled — we don't
    // track those NPCs).
    private static final int HP_CLAMP_LO = 50, HP_CLAMP_HI = 30_000;
    private static final int OFFENCE_CLAMP_LO = 50, OFFENCE_CLAMP_HI = 5_000;
    private static final int DEFENCE_CLAMP_LO = 50, DEFENCE_CLAMP_HI = 20_000;

    private static int clampPartySize(int partySize) {
        return Math.max(1, Math.min(100, partySize));
    }

    private static int highestComLevel(int maxCombat) {
        return Math.max(60, Math.min(126, maxCombat));
    }

    private static int highestHp(int maxHp) {
        // 55 + ⌊44·maxHp / 99⌋, then clamped to [55, 99].
        int v = 55 + 44 * maxHp / 99;
        return Math.max(55, Math.min(99, v));
    }

    protected int getScaledDefence(int baseDef, int partySize, int maxHp) {
        int hh = highestHp(maxHp);
        int psm1 = clampPartySize(partySize) - 1;
        int defensive = baseDef * hh / 99;
        int scalePct = 100 + (int) Math.sqrt(psm1) + psm1 * 7 / 10;
        defensive = defensive * scalePct / 100;
        if (isCm) {
            defensive = defensive * 3 / 2;
        }
        return Math.max(DEFENCE_CLAMP_LO, Math.min(DEFENCE_CLAMP_HI, defensive));
    }

    protected int getScaledOffence(int baseStat, int partySize, int maxHp) {
        int hh = highestHp(maxHp);
        int psm1 = clampPartySize(partySize) - 1;
        int offensive = baseStat * hh / 99;
        int scalePct = 100 + (int) Math.sqrt(psm1) * 7 + psm1;
        offensive = offensive * scalePct / 100;
        if (isCm) {
            offensive = offensive * 3 / 2;
        }
        return Math.max(OFFENCE_CLAMP_LO, Math.min(OFFENCE_CLAMP_HI, offensive));
    }

    // TODO: override for olm
    protected int getScaledHealth(int baseHp, int maxCombat, int partySize) {
        int hcl = highestComLevel(maxCombat);
        int ps = clampPartySize(partySize);
        int hp = baseHp * hcl / 126;
        // hp *= 1 + ⌊partySize/2⌋  (since ⌊partySize·50/100⌋ = ⌊partySize/2⌋)
        hp += hp * (ps * 50 / 100);
        if (isCm) {
            hp = hp * 3 / 2;
        }
        return Math.max(HP_CLAMP_LO, Math.min(HP_CLAMP_HI, hp));
    }
}
