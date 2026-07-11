package com.example.enemydata.misc;

import com.example.enemydata.Enemy;
import net.runelite.api.NPC;

/**
 * Abyssal Sire — the boss of the Abyssal Nexus.
 *
 * <h2>Stats</h2>
 * <table>
 * <tr>
 * <th>HP</th>
 * <th>Att</th>
 * <th>Str</th>
 * <th>Def</th>
 * <th>OffAtt</th>
 * <th>OffStr</th>
 * <th>Stab</th>
 * <th>Slash</th>
 * <th>Crush</th>
 * <th>Modifier</th>
 * </tr>
 * <tr>
 * <td>425</td>
 * <td>180</td>
 * <td>136</td>
 * <td>250</td>
 * <td>65</td>
 * <td>0</td>
 * <td>40</td>
 * <td>60</td>
 * <td>50</td>
 * <td>1.125</td>
 * </tr>
 * </table>
 *
 * <p>
 * All phases share the same stats and XP modifier (1.125 / 12.5%).
 * This is peculiar as the def{Stab,Slash,Crush} bonuses are lowered. This
 * seems not to be accounted for in the xp modifier.
 * </p>
 */
public class AbyssalSire extends Enemy {
    public AbyssalSire(NPC npc) {
        super(npc,
                425, // baseHealth
                180, // attack
                136, // str
                250, // def
                65, // offAtt
                0, // offStr
                40, // defStab
                60, // defSlash
                50 // defCrush
        );
    }
}
