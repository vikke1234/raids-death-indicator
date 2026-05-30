package com.example.enemydata;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public abstract class Enemy implements IEnemy {
    public static final Set<Integer> blacklist = Set.of(NpcID.TOA_KEPHRI_SHIELD_SCARAB, NpcID.TOA_ZEBAK_JUG, NpcID.TOA_ZEBAK_JUG_ROLLING);
    public static final Set<Integer> bosses;

    @Getter(onMethod_ = {@Synchronized})
    @Setter(onMethod_ = {@Synchronized})
    public int queuedDamage;
    public boolean shouldDraw;
    @Getter(onMethod_ = {@Synchronized})
    protected boolean hideOnDeath;

    public final int baseHealth;
    // TODO: this may require some rework, akkha for example computes the xp modifier before rounding hp
    public int scaledHealth;
    @Getter(onMethod_ = {@Synchronized})
    public int currentHealth;

    protected int attack;
    protected int str;
    protected int def;
    protected int offAtt;
    protected int offStr;
    protected int defStab;
    protected int defSlash;
    protected int defCrush;

    @Getter
    @Setter
    NPC npc;

    @Setter
    @Getter
    Client client;

    static {
        bosses = new HashSet<>();
        // Add the NpcID values to the Set
        bosses.add(NpcID.AKKHA_SPAWN);
        bosses.add(NpcID.AKKHA_MELEE);
        bosses.add(NpcID.AKKHA_RANGE);
        bosses.add(NpcID.AKKHA_MAGE);
        bosses.add(NpcID.AKKHA_ENRAGE_SPAWN);
        bosses.add(NpcID.AKKHA_ENRAGE_INITIAL);
        bosses.add(NpcID.AKKHA_ENRAGE);
        bosses.add(NpcID.AKKHA_ENRAGE_DUMMY);

        bosses.add(NpcID.TOA_BABA);
        bosses.add(NpcID.TOA_BABA_COFFIN);
        bosses.add(NpcID.TOA_BABA_DIGGING);

        bosses.add(NpcID.TOA_KEPHRI_BOSS_SHIELDED);
        bosses.add(NpcID.TOA_KEPHRI_BOSS_ENRAGE);

        bosses.add(NpcID.TOA_ZEBAK);
        bosses.add(NpcID.TOA_ZEBAK_ENRAGED);

        bosses.add(NpcID.TOA_WARDENS_P1_OBELISK_NPC);
        bosses.add(NpcID.TOA_WARDEN_ELIDINIS_PHASE2_MAGE);
        bosses.add(NpcID.TOA_WARDEN_ELIDINIS_PHASE2_RANGE);
        bosses.add(NpcID.TOA_WARDEN_ELIDINIS_PHASE3);
        bosses.add(NpcID.TOA_WARDEN_TUMEKEN_PHASE2_MAGE);
        bosses.add(NpcID.TOA_WARDEN_TUMEKEN_PHASE2_RANGE);
        bosses.add(NpcID.TOA_WARDEN_TUMEKEN_PHASE3);

        bosses.add(NpcID.RAIDS_VESPULA_PORTAL);
        bosses.add(NpcID.RAIDS_VASANISTIRIO_WALKING);
        bosses.add(NpcID.OLM_HAND_RIGHT);
        bosses.add(NpcID.OLM_HAND_RIGHT_SPAWNING);
        // TODO: add vangs? might be worth adding an event to send the HP to other players too
    }

    protected Enemy(NPC npc, int baseHealth, int attack, int str, int def,
          int offAtt, int offStr,
          int defStab, int defSlash, int defCrush) {
        this.npc = npc;

        this.baseHealth = baseHealth;
        this.currentHealth = baseHealth;
        this.attack = attack;
        this.str = str;
        this.def = def;
        this.offAtt = offAtt;
        this.offStr = offStr;
        this.defStab = defStab;
        this.defSlash = defSlash;
        this.defCrush = defCrush;
        this.shouldDraw = false;
        this.hideOnDeath = false;
        this.queuedDamage = 0;
    }

    public synchronized void setCurrentHealth(int hp) {
        if (hp >= 0) {
            currentHealth = hp;
        }
    }

    public synchronized int hit(int damage) {
        queuedDamage = Math.max(0, queuedDamage - damage);
        currentHealth -= damage;
        if (queuedDamage == 0) {
            // Queued prediction has been fully consumed by landed hitsplats.
            // shouldDraw was set when the prediction was made; clear it now that
            // the predicted event has resolved.
            shouldDraw = false;
        }
        return currentHealth;
    }

    public synchronized int heal(int amount) {
        // Cap at scaledHealth — RuneLite reports the full requested heal value
        // on hitsplats (e.g. Vanguards' group-heal mechanic), but the game
        // itself never exceeds max HP. Without this clamp, a Vanguard at 104
        // hp receiving a HEAL 114 ends up at 218 (max is 180).
        currentHealth = Math.min(currentHealth + amount, scaledHealth);
        // Heals (including phase-transition heals like Akkha's) invalidate any
        // standing prediction.
        shouldDraw = false;
        return currentHealth;
    }

    /**
     * Queues damage and counts if the enemy will die to it.
     *
     * <p>Pure state mutation — the caller is responsible for any client-thread
     * side effects (e.g. {@code npc.setDead(...)}) based on the returned value,
     * since this method can be invoked from the network thread.</p>
     *
     * @param damage damage dealt.
     * @return true if the mob died, false if not.
     */
    public synchronized boolean queueDamage(int damage) {
        queuedDamage += damage;
        return queuedDamage >= currentHealth;
    }

    public synchronized boolean shouldHighlight() {
        return shouldDraw;
    }

    public double getModifier() {
        return computeModifier(getAvgDef(), offStr, offAtt);
    }

    /**
     * Modifier formula with overridable offensive bonuses (defensive bonuses
     * default to the standard {@code (stab+slash+crush)/3} average).
     */
    protected double computeModifier(int offStrength, int offAttack) {
        return computeModifier(getAvgDef(), offStrength, offAttack);
    }

    /**
     * Modifier formula with all variable inputs explicit — for subclasses whose
     * defensive bonuses ALSO depend on runtime state (e.g. Vanguards have
     * completely different stab/slash/crush per combat style).
     */
    protected double computeModifier(int avgDef, int offStrength, int offAttack) {
        double avgs = Math.floor((getAvgLevel() * (avgDef + offStrength + offAttack)) / 5120d);
        avgs /= 40d;
        return Math.max(1.0d, 1 + avgs);
    }

    public int getScaledHealth() {
        return scaledHealth;
    }

    private int getAvgLevel() {
        return (attack + str + def + Math.min(getScaledHealth(), 2000)) / 4;
    }

    private int getAvgDef() {
        return (defStab + defSlash + defCrush) / 3;
    }
}
