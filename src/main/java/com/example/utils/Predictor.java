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
        roots = new HashMap<>();
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
    public void reset() {
        roots.clear();
    }

    /**
     * Finds the hit that most closely represents the xp drop.
     * @param xp amount of xp received
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
     * Computes the precise xp drop as a fixed length integer
     * <a href="https://oldschool.runescape.wiki/w/Combat#Experience_gain">OSRS Wiki</a>
     * @param hit Damage dealt
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
     * @param hit Damage dealt
     * @param properties Properties of the xp drop
     * @return An integer that has been rounded down to represent the xp.
     */
    public static int computeDrop(int hit, Properties properties) {
        return computePrecise(hit, properties) / 10;
    }

    /**
     * Checks if the tree for a skill is accurate or not. The tree
     * is accurate if there's only one leaf that is not dead in the tree.
     * @param skill skill to check
     * @return true if accurate, false if not
     */
    public boolean isAccurate(Skill skill) {
        PredictionTree root = roots.getOrDefault(skill, null);
        if (root == null) {
            return false;
        }
        int frac = root.getFrac();
        return frac >= 0;
    }

    public boolean isDead(Skill skill) {
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
     * @param xp amount of xp received
     * @param scaling scaling of the monster attacked
     * @param props properties related to attack
     */
    public void insertInto(int xp, double scaling, @NonNull Properties props) {
        if (!roots.containsKey(props.skill)) {
            roots.put(props.skill, PredictionTree.createRoot());
        }
        PredictionTree root = roots.get(props.skill);
        root.insertInto(xp, props);
    }

    /**
     * Predicts a hit, if the tree isn't accurate it falls back to primitive methods.
     * @param xp xp received
     * @param props properties of the hit
     * @return expected hit
     */
    public int treePredict(int xp, @NonNull Properties props) {
        if (!roots.containsKey(props.skill)) {
            roots.put(props.skill, PredictionTree.createRoot());
        }
        PredictionTree root = roots.get(props.skill);
        int frac = root.getFrac();
        root.insertInto(xp, props);
        Hit hit = findHit(xp, props);

        int next = computePrecise(hit.hit + 1, props);
        int high = computePrecise(hit.hit, props);
        int low = computePrecise(hit.hit-1, props);

        if (frac != -1) {
            if ((high + frac) / 10 == xp) {
                return hit.hit;
            }
            if ((low + frac) / 10 == xp) {
                return hit.hit-1;
            }
        }
        // if the xp drops do not overlap, check which one matches the given drop, otherwise fall back
        // to where the low hit is returned.
        if (high / 10 != low / 10) {
            boolean overlapping = (high / 10 == xp && (high / 10 - 1) != low / 10);
            int rethit = overlapping ? hit.hit : hit.hit - 1;
            if (low / 10 > 0 && rethit < 1) {
                rethit++;
            }
            return Math.max(rethit, 0);
        }

        // If the next xp drop is further than 1 xp off, we can lazily check if it wrapped or not
        if ((high / 10 + 1) == xp && (next / 10) != xp) {
            return hit.hit;
        }

        // We have to always take hit-1 because low hits have overlapping xpdrops
        // it's worse if we say that the mob died and it didn't
        return Math.max(hit.hit-1, 1);
    }
}
