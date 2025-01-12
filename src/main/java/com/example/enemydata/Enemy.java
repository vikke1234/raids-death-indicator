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
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class Enemy implements IEnemy {
    public static Map<Integer, TriFunction<NPC, Integer, Integer, Integer, Enemy>> enemies;
    public final NPCStats stats;

    @Getter
    @Setter
    NPC npc;

    static {
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
        if (pathLevel < 0) {
            // Something went wrong with the widget things
            stats = NPCStats.builder(baseHealth, isPuzzle)
                    .attack(attack)
                    .str(str)
                    .def(def)
                    .offAtt(offAtt)
                    .offStr(offStr)
                    .defStab(defStab)
                    .defSlash(defSlash)
                    .defCrush(defCrush)
                    .build();
        } else {
            stats = NPCStats.builder(invocation, partySize, pathLevel, baseHealth, isPuzzle)
                    .attack(attack)
                    .str(str)
                    .def(def)
                    .offAtt(offAtt)
                    .offStr(offStr)
                    .defStab(defStab)
                    .defSlash(defSlash)
                    .defCrush(defCrush)
                    .build();
        }
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

    public double getModifier() {
        // TODO: verify that it can't be less than 0 (e.g. swarms)
        return Math.max(stats.getModifier(), 1.0d);
    }

    public synchronized int hit(int damage) {
        return stats.hit(damage);
    }

    public synchronized int getQueuedDamage() {
        return stats.queuedDamage;
    }

    /**
     * Queues damage and counts if the enemy will die to it.
     * @param damage damage dealt.
     * @return true if the mob died, false if not.
     */
    public synchronized boolean queueDamage(int damage) {
        boolean died = stats.queueDamage(damage);
        if (died) {
            log.info("marking dead");
        }
        npc.setDead(died);
        return died;
    }

    public boolean shouldHighlight() {
        return false;
    }

    public void fixupStats(int invo, int partySize, int pathLevel) {
        // Should only happen before any npcs actually have been damaged
        stats.fixup(invo, partySize, pathLevel);
    }
}
