package com.example.enemydata;

import com.example.enemydata.ampken.*;
import com.example.enemydata.crondis.Crocodile;
import com.example.enemydata.crondis.Jug;
import com.example.enemydata.crondis.Zebak;
import com.example.enemydata.het.Akkha;
import com.example.enemydata.het.AkkhaShadow;
import com.example.enemydata.scabaras.*;
import com.example.enemydata.wardens.*;
import com.example.utils.TriFunction;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class Enemy implements IEnemy {
    public static Map<Integer, TriFunction<NPC, Integer, Integer, Integer, Enemy>> enemies;
    public int queuedDamage;
    public boolean shouldDraw;

    public int invocation;
    public int partySize;
    public int pathLevel;

    public final int base_health;
    // TODO: this may require some rework, akkha for example computes the xp modifier before rounding hp
    public int scaled_health;
    public int current_health;

    private final int attack;
    private final int str;
    private final int def;
    private final int offAtt;
    private final int offStr;
    private final int defStab;
    private final int defSlash;
    private final int defCrush;
    private final boolean isPuzzle;

    private static final Map<Integer, Double> teamScaling;
    private static final Map<Integer, Double> pathScaling;

    @Getter
    @Setter
    NPC npc;

    @Setter
    @Getter
    Client client;

    static {
        teamScaling = new HashMap<>();
        teamScaling.put(1, 1.0);
        teamScaling.put(2, 1.9);
        teamScaling.put(3, 2.8);
        teamScaling.put(4, 3.4);
        teamScaling.put(5, 4.0);
        teamScaling.put(6, 4.6);
        teamScaling.put(7, 5.2);
        teamScaling.put(8, 5.8);

        pathScaling = new HashMap<>();
        pathScaling.put(0, 1.0);
        pathScaling.put(1, 1.08);
        pathScaling.put(2, 1.13);
        pathScaling.put(3, 1.18);
        pathScaling.put(4, 1.23);
        pathScaling.put(5, 1.28);
        pathScaling.put(6, 1.33);

        enemies = new HashMap<>();
        enemies.put(NpcID.AKKHA, Akkha::new);
        enemies.put(NpcID.AKKHA_11790, Akkha::new);
        enemies.put(NpcID.AKKHA_11791, Akkha::new);
        enemies.put(NpcID.AKKHA_11792, Akkha::new);
        enemies.put(NpcID.AKKHA_11793, Akkha::new);
        enemies.put(NpcID.AKKHA_11794, Akkha::new);
        enemies.put(NpcID.AKKHA_11795, Akkha::new);
        enemies.put(NpcID.AKKHA_11796, Akkha::new);

        enemies.put(NpcID.AKKHAS_SHADOW, AkkhaShadow::new);

        enemies.put(NpcID.BABA, Baba::new);
        enemies.put(NpcID.BABA_11779, Baba::new);
        enemies.put(NpcID.BABA_11780, Baba::new);
        enemies.put(NpcID.BABOON, Baboon::new);
        enemies.put(NpcID.BOULDER_11782, Boulder::new);
        enemies.put(NpcID.BOULDER_11783, Boulder::new);

        enemies.put(NpcID.KEPHRI, Kephri::new);
        enemies.put(NpcID.KEPHRI_11721, Kephri721::new);
        enemies.put(NpcID.AGILE_SCARAB, AgileScarab::new);
        enemies.put(NpcID.ARCANE_SCARAB, Arcane::new);
        enemies.put(NpcID.SCARAB, Scarab::new);
        enemies.put(NpcID.SOLDIER_SCARAB, Soldier::new);
        enemies.put(NpcID.SPITTING_SCARAB, Spitter::new);
        enemies.put(NpcID.SCARAB_SWARM_11723, Swarm::new);

        enemies.put(NpcID.ZEBAK_11730, Zebak::new);
        enemies.put(NpcID.ZEBAK_11732, Zebak::new);
        enemies.put(NpcID.CROCODILE_11705, Crocodile::new);
        enemies.put(NpcID.JUG, Jug::new);
        enemies.put(NpcID.JUG_11736, Jug::new);

        enemies.put(NpcID.BABOON_BRAWLER, Brawler::new);
        enemies.put(NpcID.BABOON_BRAWLER_11712, Brawler::new);
        enemies.put(NpcID.BABOON_THROWER, Thrower::new);
        enemies.put(NpcID.BABOON_THROWER_11713, Thrower::new);
        enemies.put(NpcID.BABOON_MAGE, Mage::new);
        enemies.put(NpcID.BABOON_MAGE_11714, Mage::new);
        enemies.put(NpcID.BABOON_THRALL, Thrall::new);
        enemies.put(NpcID.BABOON_SHAMAN, Shaman::new);
        enemies.put(NpcID.CURSED_BABOON, Cursed::new);
        enemies.put(NpcID.VOLATILE_BABOON, Volatile::new);

        enemies.put(NpcID.OBELISK_11751, Obelisk::new);
        // different overheads -> different ID
        enemies.put(NpcID.ELIDINIS_WARDEN_11753, ElidinisWarden::new);
        enemies.put(NpcID.ELIDINIS_WARDEN_11754, ElidinisWarden::new);
        enemies.put(NpcID.ELIDINIS_WARDEN_11761, ElidinisWarden761::new);
        // different overheads -> different ID
        enemies.put(NpcID.TUMEKENS_WARDEN_11756, TumekensWarden::new);
        enemies.put(NpcID.TUMEKENS_WARDEN_11757, TumekensWarden::new);
        enemies.put(NpcID.TUMEKENS_WARDEN_11762, TumekensWarden762::new);
    }

    protected Enemy(NPC npc, int invocation, int partySize, int pathLevel,
          int baseHealth, int attack, int str, int def,
          int offAtt, int offStr,
          int defStab, int defSlash, int defCrush, boolean isPuzzle) {
        assert(partySize <= 8 && partySize >= 1);
        assert((invocation % 5) == 0);
        this.npc = npc;

        if (pathLevel >= 0) {
            this.invocation = invocation;
            this.partySize = partySize;
            this.pathLevel = pathLevel;
        }

        this.base_health = baseHealth;
        this.attack = attack;
        this.str = str;
        this.def = def;
        this.offAtt = offAtt;
        this.offStr = offStr;
        this.defStab = defStab;
        this.defSlash = defSlash;
        this.defCrush = defCrush;
        this.isPuzzle = isPuzzle;
        this.shouldDraw = false;
    }
    protected Enemy(NPC npc, int invocation, int partySize, int pathLevel,
          int baseHealth, int attack, int str, int def,
          int offAtt, int offStr,
          int defStab, int defSlash, int defCrush
    ) {
        this(npc, invocation, partySize, pathLevel,
                baseHealth, attack, str, def,
                offAtt, offStr,
                defStab, defSlash, defCrush, false);
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
        npc.setDead(died);
        return died;
    }

    public boolean shouldHighlight() {
        return false;
    }

    public void fixupStats(int invocation, int partySize, int pathLevel) {
        // Should only happen before any npcs actually have been damaged
        this.invocation = invocation;
        this.partySize = partySize;
        this.pathLevel = pathLevel;
        scaled_health = getScaledHealth(invocation, partySize, pathLevel, base_health, isPuzzle);
        current_health = scaled_health;
    }

    public double getModifier() {
        double avgs = Math.floor((getAvgLevel() * (getAvgDef() + offStr + offAtt)) / 5120d);
        avgs /= 40d;
        return 1 + avgs;
    }

    public int getScaledHealth() {
        return scaled_health;
    }

    private static int getScaledHealth(int invocation, int partySize, int pathLevel, int base_health, boolean isPuzzle) {
        double scale = (1 + 0.004 * invocation);
        double teamScale = teamScaling.get(partySize);
        double pathScale;
        if (isPuzzle) {
            pathScale = 1d;
            teamScale = 1d;
            scale = 1d;
        } else {
            pathScale = pathScaling.get(pathLevel);
        }

        return (int) (base_health * scale * teamScale * pathScale);
    }
    private int getAvgLevel() {
        return (attack + str + def + Math.min(getScaledHealth(), 2000)) / 4;
    }

    private int getAvgDef() {
        return (defStab + defSlash + defCrush) / 3;
    }

}
