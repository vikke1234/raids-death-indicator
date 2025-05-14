package com.example.utils;

import com.example.events.EntityDamaged;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.kit.KitType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Singleton
public class AnimationIdentifier {
    /// Linked list to easily remove entires from the start of the list without
    /// having to shift everything back
    /// <p>
    /// It is imperative that the user manually synchronize on
    /// the returned multimap when accessing any of its collection views:
    final Multimap<Long, Attack> playerAttacks = Multimaps.synchronizedMultimap(LinkedListMultimap.create());

    /// Current tick in the instance, starts whenever a raid for example starts
    AtomicInteger tick = new AtomicInteger(0);

    @Inject
    PartyService party;

    @Inject
    Client client;

    static Map<Integer, Function<Integer, Integer>> delayFormulas;

    /// Max tick delay before it gets filtered out.
    /// Number taken from https://oldschool.runescape.wiki/w/Hit_delay,
    /// 6 being the largest projectile delay (Spells).
    static final int MAX_DELAY = 6;

    static {
        delayFormulas = new HashMap<>();
        Function<Integer, Integer> range = distance -> 1 + (3 + distance) / 6;
        Function<Integer, Integer> chins = distance -> 1 + (distance / 6);
        delayFormulas.put(ItemID.TUMEKENS_SHADOW, distance -> 2 + (1 + distance) / 3);
        delayFormulas.put(ItemID.TONALZTICS_OF_RALOS_CHARGED, distance -> 2);
        delayFormulas.put(ItemID.OSMUMTENS_FANG_ORNAMENT, distance -> 0);
    }

    public static int calculateDelay(int weaponID, int distance) {
        if (!delayFormulas.containsKey(weaponID)) {
            return -1;
        }

        Function<Integer, Integer> formula = delayFormulas.get(weaponID);

        return formula.apply(distance);
    }
    /**
     * We need to check for attack animations in order to be able to tell
     * if an attack should've already happened or not.
     * <p>
     * This is done through calculating projectile delays using formulas from the
     * <a href="https://oldschool.runescape.wiki/w/Hit_delay">OSRS wiki</a>
     * @param ev Event
     */
    @Subscribe
    public void onAnimationChanged(AnimationChanged ev) {
        Actor actor = ev.getActor();
        Actor interacted = actor.getInteracting();
        if (actor instanceof Player && !actor.equals(client.getLocalPlayer()) && interacted instanceof NPC) {
            Player player = (Player) actor;
            PartyMember member = party.getMemberByDisplayName(player.getName());

            if (member != null) {
                PlayerComposition composition = player.getPlayerComposition();
                int distance = player.getWorldLocation().distanceTo(actor.getWorldLocation());
                int delay = AnimationIdentifier.calculateDelay(composition.getEquipmentId(KitType.WEAPON), distance);
                Attack att = new Attack(delay, tick.get());
                playerAttacks.put(member.getMemberId(), att);
            }
        }
    }

    /**
     * Keeps track of the timer.
     * @param ev Event
     */
    @Subscribe
    public void onGameTick(GameTick ev) {
        if (tick.get() < Integer.MAX_VALUE) {
            tick.getAndAdd(1);
            System.out.println("Tick: " + tick.get());
        } else {
            tick.set(0); // start from 0 again TODO: add behavior for when it actually does wrap.
        }

        synchronized (this) {
            int t = tick.get();
            for (long id : playerAttacks.keys()) {
                // Filter out any attack older than 6 ticks
                playerAttacks.get(id).removeIf(attack -> attack.getTick() - t > MAX_DELAY);
            }
        }
    }

    /**
     * Finds the attack that maps to the event sent.
     *
     * @param ev Entity damaged event
     * @return Attack if found, null otherwise
     */
    public Attack findAttack(EntityDamaged ev) {
        synchronized (playerAttacks) {
            // Here we look for attacks that *should* be correct.
            // In the case where we've logged out, we re-sync the clock in animationIdentifier.
            // If the attack is in the past, we simply ignore it. This can happen due to ping
            // or other delays for various reasons.
            List<Attack> attacks =  (List<Attack>) playerAttacks.get(ev.getMemberId());
            if (attacks.isEmpty()) {
                return null;
            }


            // TODO: is this correct? I think it needs searching in case it wasn't functioning properly
            Attack attack = attacks.get(0);
            attacks.remove(0);

            if (attack.getTick() != ev.getTick()) {
                // Re-synchronize clock in case of logouts, there shouldn't
                // be any way to get ahead of the clock, I hope...
                getAndAddTick(ev.getTick() - attack.getTick());
            }
            return attack;
        }
    }

    public int getTick() {
        return tick.get();
    }

    public void getAndAddTick(int delta) {
        this.tick.getAndAdd(delta);
    }

    public void setTick(int tick) {
        this.tick.set(tick);
    }
}
