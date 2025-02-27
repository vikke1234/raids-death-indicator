package com.example.enemydata;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public abstract class Enemy implements IEnemy {
    public static final Set<Integer> blacklist = Set.of(NpcID.SCARAB_SWARM_11723, NpcID.JUG, NpcID.JUG_11736);
    public static final Set<Integer> bosses;

    public int queuedDamage;
    public boolean shouldDraw;
    protected boolean hideOnDeath;

    public final int base_health;
    // TODO: this may require some rework, akkha for example computes the xp modifier before rounding hp
    public int scaled_health;
    @Getter
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
        bosses.add(NpcID.AKKHA);
        bosses.add(NpcID.AKKHA_11790);
        bosses.add(NpcID.AKKHA_11791);
        bosses.add(NpcID.AKKHA_11792);
        bosses.add(NpcID.AKKHA_11793);
        bosses.add(NpcID.AKKHA_11794);
        bosses.add(NpcID.AKKHA_11795);
        bosses.add(NpcID.AKKHA_11796);

        bosses.add(NpcID.BABA);
        bosses.add(NpcID.BABA_11779);
        bosses.add(NpcID.BABA_11780);

        bosses.add(NpcID.KEPHRI);
        bosses.add(NpcID.KEPHRI_11721);

        bosses.add(NpcID.ZEBAK_11730);
        bosses.add(NpcID.ZEBAK_11732);


        bosses.add(NpcID.OBELISK_11751);
        bosses.add(NpcID.ELIDINIS_WARDEN_11753);
        bosses.add(NpcID.ELIDINIS_WARDEN_11754);
        bosses.add(NpcID.ELIDINIS_WARDEN_11761);
        bosses.add(NpcID.TUMEKENS_WARDEN_11756);
        bosses.add(NpcID.TUMEKENS_WARDEN_11757);
        bosses.add(NpcID.TUMEKENS_WARDEN_11762);

        bosses.add(NpcID.ABYSSAL_PORTAL);
        bosses.add(NpcID.GREAT_OLM_RIGHT_CLAW);
        bosses.add(NpcID.GREAT_OLM_RIGHT_CLAW_7553);
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

    public synchronized int getQueuedDamage() {
        return queuedDamage;
    }

    public synchronized void setQueuedDamage(int queuedDamage) {
        this.queuedDamage = queuedDamage;
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
