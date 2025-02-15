package com.example.enemydata.toa;

import com.example.enemydata.Enemy;
import com.example.enemydata.toa.ampken.*;
import com.example.enemydata.toa.crondis.Crocodile;
import com.example.enemydata.toa.crondis.Zebak;
import com.example.enemydata.toa.het.Akkha;
import com.example.enemydata.toa.het.AkkhaShadow;
import com.example.enemydata.toa.scabaras.*;
import com.example.enemydata.toa.wardens.*;
import com.example.utils.TriFunction;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;

import java.util.HashMap;
import java.util.Map;

public class ToaEnemy extends Enemy {
    public static final Map<Integer, TriFunction<NPC, Integer, Integer, Integer, Enemy>> enemies;
    private static final Map<Integer, Double> teamScaling;
    private static final Map<Integer, Double> pathScaling;
    boolean isPuzzle;

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
        // enemies.put(NpcID.SCARAB_SWARM_11723, Swarm::new);

        enemies.put(NpcID.ZEBAK_11730, Zebak::new);
        enemies.put(NpcID.ZEBAK_11732, Zebak::new);
        enemies.put(NpcID.CROCODILE_11705, Crocodile::new);
        // enemies.put(NpcID.JUG, Jug::new);
        // enemies.put(NpcID.JUG_11736, Jug::new);

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
    public ToaEnemy(NPC npc, int invocation, int partySize, int pathLevel,
                    int baseHealth, int attack, int str, int def,
                    int offAtt, int offStr, int defStab, int defSlash, int defCrush) {
        this(npc, invocation, partySize, pathLevel,
                baseHealth, attack, str, def,
                offAtt, offStr,
                defStab, defSlash, defCrush, false);
    }

    public ToaEnemy(NPC npc, int invocation, int partySize,
                    int pathLevel, int baseHealth, int attack, int str, int def,
                    int offAtt, int offStr, int defStab, int defSlash, int defCrush, boolean isPuzzle) {
        super(npc, invocation, partySize, pathLevel,
                baseHealth, attack, str, def,
                offAtt, offStr,
                defStab, defSlash, defCrush);
        this.isPuzzle = isPuzzle;

        if (pathLevel >= 0) {
            this.invocation = invocation;
            this.partySize = partySize;
            this.pathLevel = pathLevel;
            this.scaled_health = getScaledHealth(invocation, partySize,
                    pathLevel, baseHealth);
            this.current_health = this.scaled_health;
        }
    }

    protected int getScaledHealth(int invocation, int partySize, int pathLevel, int base_health) {
        {
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
    }

    public void fixupStats(int invocation, int partySize, int pathLevel) {
        // Should only happen before any npcs actually have been damaged
        this.invocation = invocation;
        this.partySize = partySize;
        this.pathLevel = pathLevel;
        scaled_health = getScaledHealth(invocation, partySize, pathLevel, base_health);
        current_health = scaled_health;
    }
}
