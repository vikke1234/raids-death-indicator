package com.example;

import com.example.utils.PredictionTree;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Player;
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

    public boolean isAccurate() {
        return root.getFrac() != -1;
    }

    public int treePredict(int xp, Player player, Skill skill) {
        return treePredict(xp);
    }

    public int treePredict(int xp) {
        int frac = root.getFrac();
        // TODO unhardcode maxhit
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
}
