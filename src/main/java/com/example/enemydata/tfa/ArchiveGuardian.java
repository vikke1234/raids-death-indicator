package com.example.enemydata.tfa;

import com.example.enemydata.Tracked;
import com.example.enemydata.Raid;
import com.example.enemydata.TrackingMode;
import net.runelite.api.gameval.NpcID;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.gameval.VarbitID;

/**
 * The Fractured Archives — Archive Guardian.
 *
 * <p>Stats per OSRS wiki (ids 16657/16659/16660): hp 550 shield / 1100 true,
 * atk/str 250/240, def 260, off +0 att / +0 str, def stab 120,
 * slash 110/90, crush 20.</p>
 *
 * <p>{@link #currentHealth} tracks the shield while airborne and true health
 * while grounded. The boss changes id rather than despawning between states,
 * so all of that state lives on one Enemy instance.</p>
 */
@Tracked(
        name = "Archive Guardian",
        npcIds = { NpcID.TFA_GUARDIAN_START, NpcID.TFA_GUARDIAN, NpcID.TFA_GUARDIAN_UNSHIELDED },
        raid = Raid.TFA,
        defaultMode = TrackingMode.HIGHLIGHT
)
public class ArchiveGuardian extends TfaEnemy {
    private static final int SHIELD_DAMAGE_PERCENT = 40;
    private static final int TRUE_DAMAGE_PERCENT = 15;

    // OSRS wiki: Archive Guardian HP by team size.
    private static final int[] FLYING_HP = { 550, 550, 800, 1030, 1260, 1490, 1720, 1950 };
    private static final int[] GROUNDED_HP = { 1100, 1100, 1600, 2060, 2520, 2980, 3440, 3910 };

    private boolean shielded;
    private boolean phase2;
    private boolean shieldBreakPending;
    private int shieldMax;
    private int trueMaxHealth;
    private int shieldHealth;
    private int trueHealth;

    public ArchiveGuardian(NPC npc) {
        super(npc,
                npc.getId() == NpcID.TFA_GUARDIAN_UNSHIELDED ? 1100 : 550,
                250,
                240,
                260,
                0,
                0,
                120,
                npc.getId() == NpcID.TFA_GUARDIAN_UNSHIELDED ? 90 : 110,
                20);

        boolean inactive = isInactive(npc.getId());
        shielded = !inactive && npc.getId() != NpcID.TFA_GUARDIAN_UNSHIELDED;
        shieldMax = baseHealth;
        trueMaxHealth = inactive || shielded ? baseHealth * 2 : baseHealth;
        shieldHealth = shieldMax;
        trueHealth = trueMaxHealth;
        resetHealthState();
    }

    public static boolean isGuardian(int id) {
        return isInactive(id)
                || id == NpcID.TFA_GUARDIAN
                || id == NpcID.TFA_GUARDIAN_UNSHIELDED;
    }

    private static boolean isInactive(int id) {
        return id == NpcID.TFA_GUARDIAN_START || id == NpcID.TFA_GUARDIAN_DEAD;
    }

    public void syncFromClient(Client client) {
        loadMaxHealth(client);
        shieldHealth = shieldMax;
        trueHealth = trueMaxHealth;
        resetHealthState();
        shieldBreakPending = false;
    }

    public int getShieldHealth() {
        return shieldHealth;
    }

    public int getTrueHealth() {
        return trueHealth;
    }

    public boolean isShielded() {
        return shielded;
    }

    /**
     * The first grounding starts phase 2; returning to 16657/16658 starts a
     * fresh encounter with full true HP.
     */
    public synchronized void onNpcChanged(int oldId, int newId, Client client) {
        if (isInactive(newId)) {
            loadMaxHealth(client);
            shielded = false;
            phase2 = false;
            shieldHealth = 0;
            trueHealth = trueMaxHealth;
            clearPrediction();
            resetHealthState();
            return;
        }

        if (isInactive(oldId) && newId == NpcID.TFA_GUARDIAN) {
            enterFlying(client, true);
            return;
        }

        boolean newShielded = newId != NpcID.TFA_GUARDIAN_UNSHIELDED;
        if (shielded == newShielded) {
            return;
        }

        if (newShielded) {
            enterFlying(client, false);
        } else {
            phase2 = true;
            clearPrediction();
            int hp = client.getVarbitValue(VarbitID.HPBAR_HUD_HP);
            shieldHealth = 0;
            trueHealth = hp > 0 ? hp : trueHealth;
            shielded = false;
            resetHealthState();
        }
    }

    /**
     * A flying boss is highlighted when queued damage will break its shield,
     * not just when it will die. The highlight lasts until the boss actually
     * changes to the grounded composition.
     */
    @Override
    public synchronized boolean shouldHighlight() {
        return shieldBreakPending || super.shouldHighlight();
    }

    @Override
    public synchronized boolean queueDamage(int damage) {
        boolean lethal = super.queueDamage(damage);
        if (shielded && shieldHealth > 0 && queuedDamage >= shieldHealth) {
            shieldBreakPending = true;
        }
        // A shield break is not a death, so never hide the boss for it.
        return shielded ? false : lethal;
    }

    @Override
    public synchronized int hit(int damage) {
        queuedDamage = Math.max(0, queuedDamage - damage);

        if (shielded) {
            int absorbed = Math.min(shieldHealth, damage);
            shieldHealth -= absorbed;
            trueHealth -= damage - absorbed;
        } else {
            trueHealth -= damage;
        }

        if (shielded && shieldHealth <= 0) {
            shieldBreakPending = true;
        }
        if (queuedDamage == 0) {
            shouldDraw = false;
        }
        resetHealthState();
        return currentHealth;
    }

    @Override
    public synchronized int heal(int amount) {
        if (shielded) {
            shieldHealth = Math.min(shieldHealth + amount, shieldMax);
        } else {
            trueHealth = Math.min(trueHealth + amount, trueMaxHealth);
        }
        shouldDraw = false;
        resetHealthState();
        return currentHealth;
    }

    @Override
    public synchronized void setCurrentHealth(int hp) {
        if (hp < 0) {
            return;
        }
        if (shielded) {
            shieldHealth = hp;
        } else {
            trueHealth = hp;
        }
        resetHealthState();
    }

    /**
     * Queues the damage caused by destroying one of the four wall guardians.
     * The boss receives a matching hitsplat on the following tick, which
     * consumes this queued damage in DamageHandler.
     */
    public synchronized void onInertGuardianDestroyed() {
        int shieldDamage = ceilPercent(shieldMax, SHIELD_DAMAGE_PERCENT);
        int trueDamageCap = ceilPercent(trueMaxHealth, TRUE_DAMAGE_PERCENT);
        int damage;

        if (!shielded) {
            damage = phase2 ? trueDamageCap : 0;
        } else {
            int remainingShield = Math.max(0, shieldHealth);
            int absorbed = Math.min(remainingShield, shieldDamage);
            damage = absorbed;
            if (phase2) {
                damage += Math.min(shieldDamage - absorbed, trueDamageCap);
            }
        }

        if (damage > 0) {
            queueDamage(damage);
        }
    }

    private void loadMaxHealth(Client client) {
        int partySize = getPartySize(client);
        shieldMax = FLYING_HP[partySize - 1];
        trueMaxHealth = GROUNDED_HP[partySize - 1];
    }

    private static int getPartySize(Client client) {
        int size = 1;
        for (int i = 1; i < 8; i++) {
            // 1-27 = health, 30 = dead, 31 = not in the room.
            int value = client.getVarbitValue(VarbitID.TFA_CLIENT_P0 + i);
            if (value >= 1 && value <= 30) {
                size++;
            }
        }
        return Math.max(1, Math.min(8, size));
    }

    private void enterFlying(Client client, boolean freshEncounter) {
        // Party-size varbits may only be populated once the fight starts.
        loadMaxHealth(client);
        shielded = true;
        shieldHealth = shieldMax;
        if (freshEncounter) {
            trueHealth = trueMaxHealth;
        } else {
            trueHealth = Math.min(trueHealth, trueMaxHealth);
        }
        clearPrediction();
        resetHealthState();
    }

    private void resetHealthState() {
        scaledHealth = shielded ? shieldMax : trueMaxHealth;
        currentHealth = shielded ? shieldHealth : trueHealth;
    }

    private void clearPrediction() {
        shieldBreakPending = false;
        queuedDamage = 0;
        shouldDraw = false;
    }

    private static int ceilPercent(int value, int percent) {
        return (value * percent + 99) / 100;
    }
}
