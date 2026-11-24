package com.example.enemydata.tfa;

import net.runelite.api.gameval.NpcID;

/**
 * TFA wall guardians that power the Archive Guardian.
 *
 * <p>These are not tracked as {@link com.example.enemydata.Enemy}s: their own
 * health is irrelevant. They are only watched for the active → destroyed
 * npc-change, at which point the damage is applied to the Archive Guardian.</p>
 */
public final class InertGuardian {
    public static final int INACTIVE = NpcID.TFA_GUARDIAN_BATTERY_INACTIVE;
    public static final int ACTIVE = NpcID.TFA_GUARDIAN_BATTERY_ACTIVE;
    public static final int DESTROYED = NpcID.TFA_GUARDIAN_BATTERY_DESTROYED;

    private InertGuardian() {
    }

    public static boolean wasDestroyed(int oldId, int newId) {
        // Burned down by the beam or killed by a player: the npc changes id
        // instead of despawning.
        return (oldId == ACTIVE || oldId == INACTIVE) && newId == DESTROYED;
    }
}
