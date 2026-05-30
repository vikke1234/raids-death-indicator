package com.example.utils;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.runelite.api.NPC;
import net.runelite.api.Skill;

import java.util.*;

/**
 * Class for computing the internal fraction in xp.
 */
public class Predictor {
    // ConcurrentHashMap so the overlay (EDT) can safely consult the per-skill
    // tree handles while the client thread is mutating them. Tree-internal state
    // is further guarded by synchronizing the methods that touch it (below).
    Map<Skill, PredictionTree> roots;

    @AllArgsConstructor
    public static class Properties {
        public Skill skill;
        public boolean isDefensive;
        public boolean isPoweredStaff;
        public NPC npc;
        public double scaling;

        public Properties(Skill skill, boolean isDefensive, boolean isPowered, double scaling) {
            this.skill = skill;
            this.isDefensive = isDefensive;
            this.isPoweredStaff = isPowered;
            this.scaling = scaling;
        }
        // TODO: add scaling here?
    }

    public Predictor() {
        roots = new java.util.concurrent.ConcurrentHashMap<>();
    }

    @Deprecated
    public Predictor(double scaling) {
        this();
    }

    @AllArgsConstructor
    public static class Hit {
        public final int hit;
        /**
         * Could be bxp, a case where there both exists for example a 17 xp drop
         * and a 16 xp drop.
         */
        public final boolean possibleBxp;
        /**
         * It must be bxp, e.g. if you've received 18 xp, and it's
         * not possible to receive it without doing 17 + 1.
         */
        public boolean bxp;
    }

    /**
     * Resets all skill prediction trees.
     */
    public synchronized void reset() {
        roots.clear();
    }

    /**
     * Finds the hit that most closely represents the xp drop.
     *
     * @param xp         amount of xp received
     * @param properties
     * @return Upper bound for the hit.
     */
    public static Hit findHit(int xp, Properties properties) {
        boolean possibleBxp = false;
        boolean bxp = false;
        int hit;
        // 200 is a high number, will cover all the possible hits,
        // perhaps one day I'll make it compute the real max hit...
        // Today is not that day.
        for (hit = 0; hit <= 200; hit++) {
            int drop = computeDrop(hit, properties);
            if (drop > xp) {
                hit--;
                bxp = true;
                break;
            }

            if (drop == xp) {
                break;
            }

            int precise = computePrecise(hit, properties);
            // Check if xp + 1 is the real xp
            possibleBxp = (drop + 1) == xp && precise % 10 != 0;
        }

        return new Hit(hit, possibleBxp, bxp);
    }

    /**
     * Computes the "Jagex" xp drop, i.e. removes the fraction
     *
     * @param precise precise xp drop
     * @return xp drop without a fraction
     */
    public static int convertToJagexDrop(int precise) {
        return precise / 10;
    }

    /**
     * Computes the precise xp drop as a fixed length integer
     * <a href="https://oldschool.runescape.wiki/w/Combat#Experience_gain">OSRS
     * Wiki</a>
     *
     * @param hit   Damage dealt
     * @param props Skill that the xp was received in
     * @return A fixed length integer for how much xp was received.
     */
    public static int computePrecise(int hit, Properties props) {
        int scaling = (int) (props.scaling * 1000); // make it an integer that gets scaled down
        int precise = 0;
        switch (props.skill) {
            // TODO: should this be removed in it's entirety?
            case DEFENCE:
                if (props.isPoweredStaff && props.isDefensive) {
                    precise = (int) (hit * 10 * scaling);
                } else {
                    // you receive 4xp per damage with melee
                    precise = (int) (hit * 10 * 4 * scaling);
                }
                break;
            case MAGIC:
                if (props.isPoweredStaff && !props.isDefensive) {
                    precise = (int) (hit * 2 * 10 * scaling);
                } else if (props.isPoweredStaff) {
                    precise = (int) (hit * 10 * 4 / 3.0d * scaling);
                }
                // TODO spells
                break;
            case ATTACK:
            case STRENGTH:
            case RANGED:
                precise = (int) (hit * 10 * 4 * scaling);
                break;

            case HITPOINTS:
                precise = (int) (hit * 10 * 4 * scaling / 3);
                break;
        }
        return precise / 1000;
    }

    /**
     * Computes the xp amount received in the way that OSRS shows it to the user.
     *
     * @param hit        Damage dealt
     * @param properties Properties of the xp drop
     * @return An integer that has been rounded down to represent the xp.
     */
    public static int computeDrop(int hit, Properties properties) {
        return computePrecise(hit, properties) / 10;
    }

    /**
     * Checks if the tree for a skill is accurate or not. The tree
     * is accurate if there's only one leaf that is not dead in the tree.
     *
     * @param skill skill to check
     * @return true if accurate, false if not
     */
    public synchronized boolean isAccurate(Skill skill) {
        PredictionTree root = roots.getOrDefault(skill, null);
        if (root == null) {
            return false;
        }
        int frac = root.getFrac();
        return frac >= 0;
    }

    public synchronized boolean isDead(Skill skill) {
        PredictionTree root = roots.getOrDefault(skill, null);
        if (root == null) {
            return false;
        }
        int frac = root.getFrac();
        return frac == -2;
    }

    /**
     * Inserts an xp node into the prediction tree.
     *
     * @param xp    amount of xp received
     * @param props properties related to attack
     */
    public synchronized void insertInto(int xp, @NonNull Properties props) {
        if (!roots.containsKey(props.skill)) {
            roots.put(props.skill, PredictionTree.createRoot());
        }
        PredictionTree root = roots.get(props.skill);
        root.insertInto(xp, props);
    }

    /**
     * Predicts a hit, if the tree isn't accurate it falls back to primitive
     * methods.
     *
     * @param xp    xp received
     * @param props properties of the hit
     * @return expected hit
     */
    public synchronized int treePredict(int xp, @NonNull Properties props) {
        if (xp == 0) {
            return 0;
        }

        PredictionTree root = roots.computeIfAbsent(props.skill, k -> PredictionTree.createRoot());
        List<PredictionTree> leaves = PredictionTree.getLeaves(root);
        Set<Integer> available = leaves.size() == 1 ? leaves.get(0).available : null;
        root.insertInto(xp, props);
        Hit hit = findHit(xp, props);

        int high = computePrecise(hit.hit, props);
        int low = computePrecise(hit.hit - 1, props);
        int highDrop = convertToJagexDrop(high);
        int lowDrop = convertToJagexDrop(low);

        // If every candidate carry in `available` produces the same displayed xp
        // for exactly one of the two hit candidates, we know which hit it was —
        // even when available has multiple elements.
        if (available != null && !available.isEmpty()) {
            boolean allHigh = true;
            boolean allLow = true;
            for (int c : available) {
                if (convertToJagexDrop(high + c) != xp)
                    allHigh = false;
                if (convertToJagexDrop(low + c) != xp)
                    allLow = false;
            }
            if (allHigh && !allLow)
                return hit.hit;
            if (allLow && !allHigh)
                return hit.hit - 1;
        }

        // Carry unknown. hit.hit is unique when hit-1's maximum possible display
        // (carry 9) still falls below xp - hit-1 can't have produced this drop with
        // any carry. Covers both the exact match (drop(hit) == xp) and bxp
        // (drop(hit) + 1 == xp) cases.
        if (highDrop != lowDrop) {
            int maxLowDrop = convertToJagexDrop(low + 9);
            boolean uniqueHigh = xp > maxLowDrop;
            return Math.max(uniqueHigh ? hit.hit : hit.hit - 1, 1);
        }

        // Candidates share a display. If xp is one above and the next hit doesn't
        // also reach it, treat as bxp on the upper candidate.
        int nextDrop = convertToJagexDrop(computePrecise(hit.hit + 1, props));
        if (highDrop + 1 == xp && nextDrop != xp) {
            return hit.hit;
        }

        // Default to hit-1: low hits have overlapping xp drops, and predicting too
        // high causes false-death indications.
        return Math.max(hit.hit - 1, 1);
    }

    /**
     * Experimental tree prediction, hopefully more maintainable and better.
     *
     * @param xp
     * @param props
     * @return
     */
    public synchronized int treePredict2(int xp, @NonNull Properties props) {
        if (xp == 0) {
            return 0;
        }

        if (!roots.containsKey(props.skill)) {
            roots.put(props.skill, PredictionTree.createRoot());
        }

        PredictionTree root = roots.get(props.skill);
        int frac = root.getFrac();
        root.insertInto(xp, props);
        Hit hit = findHit(xp, props);

        int high = computePrecise(hit.hit, props);
        int low = computePrecise(hit.hit - 1, props);

        if (frac >= 0) {
            high += frac;
            low += frac;
        }
        // if the xp drops do not overlap, check which one matches the given drop,
        // otherwise fall back
        // to where the low hit is returned.
        if (convertToJagexDrop(high) != convertToJagexDrop(low)) {
            if (convertToJagexDrop(high) == xp) {
                return hit.hit;
            }
        }

        // We have to always take hit-1 because low hits have overlapping xpdrops
        // it's worse if we say that the mob died and it didn't
        return Math.max(hit.hit - 1, 1);
    }
}
