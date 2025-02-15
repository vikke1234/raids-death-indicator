package com.example.enemydata;

import com.example.enemydata.toa.ampken.*;
import com.example.enemydata.toa.crondis.Crocodile;
import com.example.enemydata.toa.crondis.Zebak;
import com.example.enemydata.toa.het.Akkha;
import com.example.enemydata.toa.het.AkkhaShadow;
import com.example.enemydata.toa.scabaras.*;
import com.example.enemydata.toa.wardens.*;
import com.example.utils.TriFunction;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class Enemy implements IEnemy {
    public static final Set<Integer> blacklist = Set.of(NpcID.SCARAB_SWARM_11723, NpcID.JUG, NpcID.JUG_11736);
    public int queuedDamage;
    public boolean shouldDraw;
    protected boolean hideOnDeath;

    public int invocation;
    public int partySize;
    public int pathLevel;

    public final int base_health;
    // TODO: this may require some rework, akkha for example computes the xp modifier before rounding hp
    public int scaled_health;
    @Getter
    public int current_health;

    private final int attack;
    private final int str;
    private final int def;
    private final int offAtt;
    private final int offStr;
    private final int defStab;
    private final int defSlash;
    private final int defCrush;

    @Getter
    @Setter
    NPC npc;

    @Setter
    @Getter
    Client client;


    protected Enemy(NPC npc, int invocation, int partySize, int pathLevel,
          int baseHealth, int attack, int str, int def,
          int offAtt, int offStr,
          int defStab, int defSlash, int defCrush) {
        assert(partySize <= 8 && partySize >= 1);
        assert((invocation % 5) == 0);
        this.npc = npc;

        this.base_health = baseHealth;
        this.attack = attack;
        this.str = str;
        this.def = def;
        this.offAtt = offAtt;
        this.offStr = offStr;
        this.defStab = defStab;
        this.defSlash = defSlash;
        this.defCrush = defCrush;
        this.shouldDraw = false;
        this.hideOnDeath = true;
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
