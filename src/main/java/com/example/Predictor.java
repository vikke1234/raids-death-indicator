package com.example;

import com.example.utils.PredictionTree;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.runelite.api.Skill;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        // TODO: add scaling here?
    }

    public Predictor() {
        roots = new HashMap<>();
    }

    @Deprecated
    public Predictor(double scaling) {
        roots = new HashMap<>();
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
     * Finds the hit that most closely represents the xp drop.
     * @param xp
     * @param scaling
     * @param properties
     * @return
     */
    public static Hit findHit(int xp, double scaling, Properties properties) {
        boolean possibleBxp = false;
        boolean bxp = false;
        int hit;
        // 100 is a high number, will cover all the possible hits
        for (hit = 0; hit <= 100; hit++) {
            int drop = computeDrop(hit, scaling, properties);
            if (drop > xp) {
                hit--;
                bxp = true;
                break;
            }

            if (drop == xp) {
                break;
            }

            int precise = computePrecise(hit, scaling, properties);
            possibleBxp = (drop + 1) == xp && precise % 10 != 0;
        }

        return new Hit(hit, possibleBxp, bxp);
    }


    /**
     * Computes the precise xp drop as a fixed length integer
     *
     * @param hit Damage dealt
     * @param scaling Scaling to apply to the drop
     * @param props Skill that the xp was received in
     * @return A fixed length integer for how much xp was received.
     */
    public static int computePrecise(int hit, double scaling, Properties props) {
        switch (props.skill) {
            case DEFENCE:
                if (props.isPoweredStaff && props.isDefensive) {
                    return (int) (hit * 10 * scaling);
                } else {
                    // you receive 4xp per damage with melee
                    return (int) (hit * 10 * 4 * scaling);
                }
            case MAGIC:
                if (props.isPoweredStaff && !props.isDefensive) {
                    return (int) (hit * 2 * 10 * scaling); // TODO
                }
                if (props.isPoweredStaff) {
                    // We have to use BigDecimal due to floating point errors
                    BigDecimal damage = new BigDecimal(hit * 10);
                    BigDecimal mExpMultiplier = BigDecimal.ONE.add(BigDecimal.ONE.divide(BigDecimal.valueOf(3), 100,RoundingMode.HALF_UP));
                    BigDecimal finalXp = damage.multiply(mExpMultiplier).multiply(BigDecimal.valueOf(scaling));
                    return (int) finalXp.doubleValue();
                }
                break;
            case ATTACK:
            case STRENGTH:
            case RANGED:
                return (int) (hit * 10 * 4 * scaling);
        }
        return 0;
    }

    /**
     * Computes the xp amount received in the way that OSRS shows it to the user.
     *
     * @param hit Damage dealt
     * @param scaling Scaling to apply to the drop
     * @param properties Properties of the xp drop
     * @return An integer that has been rounded down to represent the xp.
     */
    public static int computeDrop(int hit, double scaling, Properties properties) {
        return computePrecise(hit, scaling, properties) / 10;
    }

    public boolean isAccurate(Skill skill) {
        PredictionTree root = roots.getOrDefault(skill, null);
        return root != null && roots.get(skill).getFrac() != -1;
    }

    public void insertInto(int xp, double scaling, @NonNull Properties props) {
        if (!roots.containsKey(props.skill)) {
            roots.put(props.skill, PredictionTree.createRoot());
        }
        PredictionTree root = roots.get(props.skill);
        root.insertInto(xp, scaling, props);
    }

    public int treePredict(int xp, double scaling, @NonNull Properties props) {
        if (!roots.containsKey(props.skill)) {
            roots.put(props.skill, PredictionTree.createRoot());
        }
        PredictionTree root = roots.get(props.skill);
        int frac = root.getFrac();
        // TODO unhardcode maxhit
        root.insertInto(xp, scaling, props);
        Hit hit = findHit(xp, scaling, props);

        if (frac != -1) {
            int high = computePrecise(hit.hit, scaling, props);
            int low = computePrecise(hit.hit-1, scaling, props);
            if ((high + frac) / 10 == xp) {
                return hit.hit;
            }
            if ((low + frac) / 10 == xp) {
                return hit.hit-1;
            }
        }

        if (computeDrop(hit.hit, scaling, props) == xp) {
            return hit.hit;
        }
        // fall back to safe bet
        return Math.max(hit.hit-1, 0);
    }
}
