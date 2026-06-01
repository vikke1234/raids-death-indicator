package com.example.enemydata.toa;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ampken.*;
import com.example.enemydata.toa.crondis.Crocodile;
import com.example.enemydata.toa.crondis.Zebak;
import com.example.enemydata.toa.het.Akkha;
import com.example.enemydata.toa.het.AkkhaShadow;
import com.example.enemydata.toa.scabaras.*;
import com.example.enemydata.toa.wardens.*;
import com.example.utils.QuadFunction;
import net.runelite.api.NPC;
import net.runelite.api.gameval.NpcID;

import java.util.HashMap;
import java.util.Map;

public class ToaEnemy extends Enemy {
    public static final Map<Integer, QuadFunction<NPC, Integer, Integer, Integer, Enemy>> enemies;
    private static final Map<Integer, Double> teamScaling;
    private static final Map<Integer, Double> pathScaling;

    protected final boolean isPuzzle;
    public int invocation;
    public int partySize;
    public int pathLevel;

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
        enemies.put(NpcID.AKKHA_SPAWN, Akkha::new);
        enemies.put(NpcID.AKKHA_MELEE, Akkha::new);
        enemies.put(NpcID.AKKHA_RANGE, Akkha::new);
        enemies.put(NpcID.AKKHA_MAGE, Akkha::new);
        enemies.put(NpcID.AKKHA_ENRAGE_SPAWN, Akkha::new);
        enemies.put(NpcID.AKKHA_ENRAGE_INITIAL, Akkha::new);
        enemies.put(NpcID.AKKHA_ENRAGE, Akkha::new);
        enemies.put(NpcID.AKKHA_ENRAGE_DUMMY, Akkha::new);

        enemies.put(NpcID.AKKHA_SHADOW, AkkhaShadow::new);

        enemies.put(NpcID.TOA_BABA, Baba::new);
        enemies.put(NpcID.TOA_BABA_COFFIN, Baba::new);
        enemies.put(NpcID.TOA_BABA_DIGGING, Baba::new);
        enemies.put(NpcID.TOA_BABA_BABOON, Baboon::new);
        enemies.put(NpcID.TOA_BABA_BOULDER, Boulder::new);
        enemies.put(NpcID.TOA_BABA_BOULDER_WEAK, Boulder::new);

        enemies.put(NpcID.TOA_KEPHRI_BOSS_SHIELDED, Kephri::new);
        enemies.put(NpcID.TOA_KEPHRI_BOSS_ENRAGE, Kephri721::new);
        enemies.put(NpcID.TOA_KEPHRI_SCARAB_RANGEKITE, AgileScarab::new);
        enemies.put(NpcID.TOA_KEPHRI_GUARDIAN_MAGE, Arcane::new);
        enemies.put(NpcID.TOA_SCABARAS_SCARAB, Scarab::new);
        enemies.put(NpcID.TOA_KEPHRI_GUARDIAN_MELEE, Soldier::new);
        enemies.put(NpcID.TOA_KEPHRI_GUARDIAN_RANGED, Spitter::new);
        // enemies.put(NpcID.TOA_KEPHRI_SCARAB_SWARM, Swarm::new);

        enemies.put(NpcID.TOA_ZEBAK, Zebak::new);
        enemies.put(NpcID.TOA_ZEBAK_ENRAGED, Zebak::new);
        enemies.put(NpcID.TOA_CRONDIS_CROCODILE, Crocodile::new);
        // enemies.put(NpcID.TOA_ZEBAK_JUG, Jug::new);
        // enemies.put(NpcID.TOA_ZEBAK_JUG_ROLLING, Jug::new);

        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1, Brawler::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_2, Brawler::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1, Thrower::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_2, Thrower::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1, Mage::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_2, Mage::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_THRALL, Thrall::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_SHAMAN, Shaman::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_CURSED, Cursed::new);
        enemies.put(NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE, Volatile::new);

        enemies.put(NpcID.TOA_WARDENS_P1_OBELISK_NPC, Obelisk::new);
        // different overheads -> different ID
        enemies.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE2_MAGE, ElidinisWarden::new);
        enemies.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE2_RANGE, ElidinisWarden::new);
        enemies.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE3, ElidinisWarden761::new);
        // different overheads -> different ID
        enemies.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE2_MAGE, TumekensWarden::new);
        enemies.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE2_RANGE, TumekensWarden::new);
        enemies.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE3, TumekensWarden762::new);
    }

    public ToaEnemy(NPC npc, int invocation, int partySize, int pathLevel,
                    int baseHealth, int attack, int str, int def,
                    int offAtt, int offStr,
                    int defStab, int defSlash, int defCrush, boolean isPuzzle) {
        super(npc, baseHealth, attack, str, def, offAtt, offStr, defStab, defSlash, defCrush);
        assert(partySize <= 8 && partySize >= 1);
        assert((invocation % 5) == 0);

        this.isPuzzle = isPuzzle;
        this.shouldDraw = false;
        this.hideOnDeath = true;
        this.queuedDamage = 0;

        if (pathLevel >= 0) {
            this.invocation = invocation;
            this.partySize = partySize;
            this.pathLevel = pathLevel;
            this.scaledHealth = getScaledHealth(invocation, partySize,
                    pathLevel, baseHealth, isPuzzle);
            this.currentHealth = this.scaledHealth;
        }

    }
    public ToaEnemy(NPC npc, int invocation, int partySize, int pathLevel,
                    int baseHealth, int attack, int str, int def,
                    int offAtt, int offStr,
                    int defStab, int defSlash, int defCrush
    ) {
        this(npc, invocation, partySize, pathLevel,
                baseHealth, attack, str, def,
                offAtt, offStr,
                defStab, defSlash, defCrush, false);
    }

    private static int getScaledHealth(int invocation, int partySize, int pathLevel, int baseHealth, boolean isPuzzle) {
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

        return (int) (baseHealth * scale * teamScale * pathScale);
    }

    public void fixupStats(int invocation, int partySize, int pathLevel) {
        // Should only happen before any npcs actually have been damaged
        this.invocation = invocation;
        this.partySize = partySize;
        this.pathLevel = pathLevel;
        scaledHealth = getScaledHealth(invocation, partySize, pathLevel, baseHealth, isPuzzle);
        currentHealth = scaledHealth;
    }
}
