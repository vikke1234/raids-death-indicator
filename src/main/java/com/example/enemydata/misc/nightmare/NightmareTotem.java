package com.example.enemydata.misc.nightmare;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import com.example.enemydata.Enemy;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;

import java.util.Set;

/**
 * The four totems / pillars in the Nightmare of Ashihama fight.
 *
 * <p>
 * HP scales with the encounter:
 * </p>
 * <ul>
 * <li>Challenge mode (varbit
 * {@code PLAYER_IS_IN_NIGHTMARE_CHALLENGE}): 200 HP, flat.</li>
 * <li>Regular mode: 300 HP base, +30 per player above 5 in the arena
 * (so 6 players = 330, 7 = 360, etc.).</li>
 * </ul>
 */
@Tracked(
        name = "Nightmare Totem",
        npcIds = { NpcID.NIGHTMARE_TOTEM_1_DORMANT, NpcID.NIGHTMARE_TOTEM_2_DORMANT, NpcID.NIGHTMARE_TOTEM_3_DORMANT, NpcID.NIGHTMARE_TOTEM_4_DORMANT, NpcID.NIGHTMARE_TOTEM_1_READY, NpcID.NIGHTMARE_TOTEM_2_READY, NpcID.NIGHTMARE_TOTEM_3_READY, NpcID.NIGHTMARE_TOTEM_4_READY },
        raid = Raid.MISC,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class NightmareTotem extends Enemy {
    /** Player-search radius around the totem (tiles). Arena fits in ~20×20. */
    private static final int NEARBY_RADIUS = 30;

    public static final Set<Integer> NPC_IDS = Set.of(
            NpcID.NIGHTMARE_TOTEM_1_DORMANT, // 9435
            NpcID.NIGHTMARE_TOTEM_2_DORMANT, // 9438
            NpcID.NIGHTMARE_TOTEM_3_DORMANT, // 9440
            NpcID.NIGHTMARE_TOTEM_4_DORMANT, // 9444
            NpcID.NIGHTMARE_TOTEM_1_READY, // 9435
            NpcID.NIGHTMARE_TOTEM_2_READY, // 9438
            NpcID.NIGHTMARE_TOTEM_3_READY, // 9440
            NpcID.NIGHTMARE_TOTEM_4_READY // 9444
    );

    public NightmareTotem(NPC npc, Client client) {
        super(npc, totemHp(npc, client), 1, 1, 1, 0, 0, 0, 0, 0);
        // TODO: only magic is 2x
        this.damageMultiplier = 2;
    }

    private static int totemHp(NPC npc, Client client) {
        if (client != null && client.getVarbitValue(VarbitID.PLAYER_IS_IN_NIGHTMARE_CHALLENGE) != 0) {
            return 200;
        }
        int extra = Math.max(0, countNearbyPlayers(npc, client) - 5) * 30;
        return 300 + extra;
    }

    private static int countNearbyPlayers(NPC npc, Client client) {
        if (client == null || npc == null) {
            return 0;
        }
        WorldPoint totem = npc.getWorldLocation();
        if (totem == null) {
            return 0;
        }
        int count = 0;
        for (Player p : client.getWorldView(-1).players()) {
            if (p == null || p.getWorldLocation() == null) {
                continue;
            }
            if (p.getWorldLocation().distanceTo(totem) <= NEARBY_RADIUS) {
                count++;
            }
        }
        return count;
    }
}
