package com.example;

import com.example.utils.PredictionTree;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Skill;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for computing the internal fraction in xp.
 */
public class Predictor {
    Set<Integer> available;
    double scaling;
    int fraction;
    List<Integer> possible;
    PredictionTree root;

    public Predictor(double scaling) {
        root = PredictionTree.createRoot();
        this.scaling = scaling;
    }

    public Predictor(int hit, int xp, double scaling) {
        possible = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            possible.add((int)Math.floor(i * 10 * scaling));
        }

        this.scaling = scaling;
        available = new HashSet<>();
        fraction = -1;
        int true_xp = (int)(hit * 10 * scaling);
        int start;
        int end;

        if ((true_xp / 10) != xp) {
            // if we got bonus xp the fraction must be between 0 and the fractional drop we got
            start = 0;
            end = getLastDigit(true_xp);
        } else {
            start = getLastDigit(true_xp);
            end = 10;
        }

        if (start == end) {
            end = 10;
        }
        // We initialize the set with all possible fractions
        for (int i = start; i < end; i++) {
            available.add(i);
        }
        System.out.println("initial predictions: " + available + "start: " + start + " end: " + end + " hit: " + hit + " xp: " + xp + " actual: " + true_xp);
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
     * @param maxHit
     * @return
     */
    public static Hit findHit(int xp, double scaling, int maxHit) {
        boolean possibleBxp = false;
        boolean bxp = false;
        int hit;
        for (hit = 0; hit <= maxHit; hit++) {
            int drop = computeDrop(hit, scaling, Skill.DEFENCE);
            if (drop > xp) {
                hit--;
                bxp = true;
                break;
            }

            if (drop == xp) {
                break;
            }

            int precise = computePrecise(hit, scaling, Skill.DEFENCE);
            possibleBxp = (drop + 1) == xp && precise % 10 != 0;
        }

        return new Hit(hit, possibleBxp, bxp);
    }


    /**
     * Computes the precise xp drop as a fixed length integer
     *
     * @param hit Damage dealt
     * @param scaling Scaling to apply to the drop
     * @param skill Skill that the xp was received in
     * @return A fixed length integer for how much xp was received.
     */
    public static int computePrecise(int hit, double scaling, Skill skill) {
        switch (skill) {
            case DEFENCE:
                return (int) (hit * 10 * scaling);
        }
        return 0;
    }

    /**
     * Computes the xp amount received in the way that OSRS shows it to the user.
     *
     * @param hit Damage dealt
     * @param scaling Scaling to apply to the drop
     * @param skill Skill that the xp was received in.
     * @return An integer that has been rounded down to represent the xp.
     */
    public static int computeDrop(int hit, double scaling, Skill skill) {
        return computePrecise(hit, scaling, skill) / 10;
    }

    private int getLastDigit(int xp) {
        return xp % 10;
    }

    public boolean computeFraction(int xp) {

        return false;
    }

    public boolean computeFraction(int hit, int xp) {
        // We will use fixed precision integers
        int scaled = (int) (hit * 10 * scaling);
        int frac = getLastDigit(scaled);

        if (fraction != -1) {
            // Track using variable due to simpler usage
            System.out.println("Damage: " + hit + " xp: " + xp + " Actual xp: " + scaled + " Frac: " + frac);
            fraction = (fraction + frac) % 10;
            return true;
        }

        boolean bonusXp = (scaled / 10) < xp;
        available = available.stream().map(n -> (n + frac) % 10).collect(Collectors.toSet());
        if (bonusXp) {
            available = available.stream().filter(n -> n < frac).collect(Collectors.toSet());
        }
        if (isAccurate()) {
            fraction = available.stream().findFirst().get(); // Can't throw, exactly 1 element in it.
        }
        System.out.println("Damage: " + hit + " xp: " + xp + " Actual xp: " + scaled + " Frac: " + available + " BXP: " + bonusXp);
        return isAccurate();
    }
    public boolean isAccurate() {
        return root.getFrac() != -1;
    }

    public int treePredict(int xp) {
        // TODO unhardcode this
        int frac = root.getFrac();
        root.insertInto(xp, scaling, 84);
        Hit hit = findHit(xp, scaling, 84);

        if (frac != -1) {
            int high = computePrecise(hit.hit, scaling, Skill.DEFENCE);
            int low = computePrecise(hit.hit-1, scaling, Skill.DEFENCE);
            if ((high + frac) / 10 == xp) {
                return hit.hit;
            }
            if ((low + frac) / 10 == xp) {
                return hit.hit-1;
            }
        }

        // fall back to safe bet
        return hit.hit-1;
    }

    public int predictHit(int xp) {
        if (true) throw new NotImplementedException("error");
        int predicted = (int) (xp / scaling);
        int rescaledXp = (int) (predicted * scaling);
        int minimumXpExact = 0;

        int i;
        for (i = 1; i < 100; i++) {
            int testedXp = (int) (i * 10 * scaling);
            if (testedXp / 10 > xp) {
                i--;
                break;
            }
            if ((testedXp + fraction) / 10 == xp) {
                break;
            }
        }

        int minimumXp = minimumXpExact / 10;
        System.out.println("\nPredicted damage: " + predicted + " xp: " + xp + " rescaled xp: " + rescaledXp + " Minimum xp: " + minimumXpExact + " minimum hit: " + (int) (minimumXp / scaling) + " Frac: "+ fraction + " Scaling: " + scaling);
        System.out.println("New: " + i + " xp: " + ((int)(i * 10 * scaling)));
        return 0;
    }

    private double round(double xp) {
        return Math.round(xp * 10d) / 10d;
    }
}
