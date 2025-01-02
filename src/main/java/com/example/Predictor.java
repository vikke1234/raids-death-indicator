package com.example;

import com.example.utils.PredictionTree;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.runelite.api.NPC;
import net.runelite.api.Skill;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Class for computing the internal fraction in xp.
 */
public class Predictor {
    Map<Skill, PredictionTree> roots;
    Set<Skill> logSkills;

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
        logSkills = new HashSet<>();
        logSkills.add(Skill.DEFENCE);
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
     * Finds the hit that most closely represents the xp drop.
     * @param xp amount of xp received
     * @param properties
     * @return Upper bound for the hit.
     */
    public static Hit findHit(int xp, Properties properties) {
        boolean possibleBxp = false;
        boolean bxp = false;
        int hit;
        // 100 is a high number, will cover all the possible hits
        for (hit = 0; hit <= 100; hit++) {
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
            case DEFENCE:
                if (props.isPoweredStaff && props.isDefensive) {
                    precise = (int) (hit * 10 * scaling);
                } else {
                    // you receive 4xp per damage with melee
                    precise = (int) (hit * 10 * 4 * scaling);
                }
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

    public boolean isAccurate(Skill skill) {
        PredictionTree root = roots.getOrDefault(skill, null);
        if (root == null) {
            return false;
        }
        int frac = root.getFrac();
        return frac != -1;
    }

    public void insertInto(int xp, double scaling, @NonNull Properties props) {
        if (!roots.containsKey(props.skill)) {
            roots.put(props.skill, PredictionTree.createRoot());
        }
        // Disable printing for this skill, TODO: remove for real release
        PrintStream original = System.out;
        if (!logSkills.contains(props.skill)) {
            PrintStream dummy = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    //noop
                }
            });
            System.setOut(dummy);
        }
        PredictionTree root = roots.get(props.skill);
        root.insertInto(xp, props);

        System.setOut(original);
    }

    public int treePredict(int xp, @NonNull Properties props) {
        if (!roots.containsKey(props.skill)) {
            roots.put(props.skill, PredictionTree.createRoot());
        }
        PredictionTree root = roots.get(props.skill);
        int frac = root.getFrac();
        root.insertInto(xp, props);
        Hit hit = findHit(xp, props);

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
            return high / 10 == xp ? hit.hit : hit.hit - 1;
        }

        // We have to always take hit-1 because low hits have overlapping xpdrops
        // it's worse if we say that the mob died and it didn't
        return Math.max(hit.hit-1, 1);
    }
}
