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
    protected boolean hideOnDeath;

    public final int base_health;
    // TODO: this may require some rework, akkha for example computes the xp modifier before rounding hp
    public int scaled_health;
    @Getter(onMethod_ = {@Synchronized})
    public int current_health;

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

        this.base_health = baseHealth;
        this.current_health = base_health;
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

    public void setCurrentHealth(int hp) {
        if (hp >= 0) {
            current_health = hp;
        }
    }

    public synchronized int hit(int damage) {
        queuedDamage = Math.max(0, queuedDamage - damage);
        current_health -= damage;
        return current_health;
    }

    /**
     * Queues damage and counts if the enemy will die to it.
     * @param damage damage dealt.
     * @return true if the mob died, false if not.
     */
    public synchronized boolean queueDamage(int damage) {
        queuedDamage += damage;
        boolean died = queuedDamage >= current_health;

        npc.setDead(died && hideOnDeath);
        return died;
    }

    public boolean shouldHighlight() {
        return shouldDraw;
    }

    public double getModifier() {
        double avgs = Math.floor((getAvgLevel() * (getAvgDef() + offStr + offAtt)) / 5120d);
        avgs /= 40d;
        return Math.max(1.0d, 1 + avgs);
    }

    public int getScaledHealth() {
        return scaled_health;
    }

    private int getAvgLevel() {
        return (attack + str + def + Math.min(getScaledHealth(), 2000)) / 4;
    }

    private int getAvgDef() {
        return (defStab + defSlash + defCrush) / 3;
    }
}
