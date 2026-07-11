package com.example.enemydata.cox;

import com.example.enemydata.Enemy;
import com.example.utils.HexFunction;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoxEnemy extends Enemy {
    public static final Map<Integer, HexFunction<NPC, Boolean, Integer, Integer, Integer, Long, CoxEnemy>> enemies;
    boolean isCm;
    int partySize;
    int maxHp;

    @Getter(onMethod_ = { @Synchronized })
    @Setter(onMethod_ = { @Synchronized })
    boolean stalled;

    @Getter(onMethod_ = { @Synchronized })
    @Setter(onMethod_ = { @Synchronized })
    long raidTick;

    static {
        enemies = new HashMap<>();

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
        enemies.put(NpcID.RAIDS_STONEGUARDIANS_LEFT, Guardian::new);
        enemies.put(NpcID.RAIDS_STONEGUARDIANS_RIGHT, Guardian::new);

        enemies.put(NpcID.RAIDS_DOGODILE_SUBMERGED, GreatMuttadile::new);
        enemies.put(NpcID.RAIDS_DOGODILE, GreatMuttadile::new);
        enemies.put(NpcID.RAIDS_DOGODILE_JUNIOR, SmallMuttadile::new);
    }

    protected CoxEnemy(NPC npc, boolean isCm, int partySize,
            int maxCombat, int maxHp, long raidTick,
            int baseHealth, int melee, int def, int offAtt, int offStr,
            int defStab, int defSlash, int defCrush) {

        super(npc, baseHealth, melee, melee, def, offAtt, offStr, defStab, defSlash, defCrush);
        assert (attack == str);
        this.isCm = isCm;
        this.partySize = partySize;
        this.maxHp = maxHp;
        this.stalled = false;
        this.raidTick = raidTick;

        this.scaledHealth = this.getScaledHealth(baseHealth, maxCombat, partySize);
        this.currentHealth = scaledHealth;
        int scaledDef = getScaledDefence(def, partySize, maxHp);
        this.def = scaledDef;
        int scaledMelee = getScaledOffence(melee, partySize, maxHp);
        this.str = scaledMelee;
        this.attack = scaledMelee;
        this.regenCycle = 100;
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

    /**
     * Increments the current raid tick for an NPC, handles regen cycles and
     * stalls.
     */
    public void incrementRaidTick() {
        if (isStalled()) {
            // If NPC is stalled we shouldn't do anything, NPC queues are
            // paused for the duration of a stall.
            return;
        }

        if (regenCycle > 0 && (raidTick % regenCycle) == 0) {
            heal(1);
        }

        // Sync the generic regen tick so the overlay and isRegenUncertain() work
        if (regenCycle > 0) {
            regenTick = (int) (raidTick % regenCycle);
        }

        if (raidTick == Long.MAX_VALUE) {
            raidTick = 0;
        } else {
            raidTick++;
        }
    }

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
