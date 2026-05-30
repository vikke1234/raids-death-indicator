package com.example;

import com.example.utils.Predictor;
import net.runelite.api.Skill;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class PredictorTests {

    private int calibrate(Predictor predictor, List<Integer> possibleDrops, Predictor.Properties properties,
            int internalFrac) {
        while (!predictor.isAccurate(properties.skill)) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            // int hit = hits[i++];
            int xp = possibleDrops.get(hit);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            // System.out.println("internal: " + internalFrac + " hit: " + hit + " xp: " +
            // xp +" wrapped: " + wrapped);
            // We don't care about uncalibrated hits
            int predicted = predictor.treePredict(xp / 10, properties);
            // Make sure that when calibrating, we won't estimate higher
            assertTrue(predicted + " <= " + hit, predicted <= hit);
        }
        return internalFrac;
    }

    private void runIterations(Predictor predictor, int iterations, List<Integer> possibleDrops,
            Predictor.Properties properties, int internalFrac) {
        for (int i = 0; i < iterations; i++) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            int xp = possibleDrops.get(hit);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            int predicted = predictor.treePredict(xp / 10, properties);
            // System.out.println("internal: " + internalFrac + " hit(" +
            // properties.skill.getName() + "): " + hit + " predicted: " + predicted + " xp:
            // " + xp +" wrapped: " + wrapped);
            // System.out.println();
            assertEquals(hit, predicted);
        }
    }

    private void runTest(Predictor.Properties properties, int iterations, int internalFrac) {
        Predictor predictor = new Predictor();
        List<Integer> possibleDrops = IntStream.rangeClosed(0, iterations)
                .map(n -> Predictor.computePrecise(n, properties))
                .boxed().collect(Collectors.toList());
        internalFrac = calibrate(predictor, possibleDrops, properties, internalFrac);

        runIterations(predictor, iterations, possibleDrops, properties, internalFrac);
    }

    @Test
    public void testPredictor() {
        double scaling = 1.375;
        int internalFrac = 9;
        int iterations = 1000;
        Predictor.Properties properties;
        properties = new Predictor.Properties(Skill.HITPOINTS, true, true, scaling);
        runTest(properties, iterations, internalFrac);
    }

    @Test
    public void testXpCalc() {
        double scaling = 1.375d;
        int hit = 10;
        Predictor.Properties properties = new Predictor.Properties(Skill.MAGIC, true, true, scaling);
        assertEquals(183, Predictor.computePrecise(hit, properties));
        properties = new Predictor.Properties(Skill.DEFENCE, true, true, scaling);
        assertEquals(137, Predictor.computePrecise(hit, properties));
    }

    @Test
    public void testPredictorMultipleStyles() {
        double scaling = 1.495;
        int mfrac = 9;
        int dfrac = 3;
        int iterations = 1000;
        Predictor predictor = new Predictor();

        Predictor.Properties magicxp = new Predictor.Properties(Skill.MAGIC, true, true, scaling);
        Predictor.Properties defxp = new Predictor.Properties(Skill.DEFENCE, true, true, scaling);
        List<Integer> validMagic = IntStream.rangeClosed(0, 100)
                .map(hit -> Predictor.computePrecise(hit, magicxp))
                .boxed()
                .collect(Collectors.toList());
        List<Integer> validDef = IntStream.rangeClosed(0, 100)
                .map(hit -> Predictor.computePrecise(hit, defxp))
                .boxed()
                .collect(Collectors.toList());

        while (!predictor.isAccurate(Skill.MAGIC) || !predictor.isAccurate(Skill.DEFENCE)) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            // int hit = hits[i++];
            int mxp = validMagic.get(hit);
            int dxp = validDef.get(hit);
            boolean mwrapped = ((mxp % 10) + mfrac) >= 10;
            boolean dwrapped = ((dxp % 10) + dfrac) >= 10;
            mxp += mwrapped ? 10 : 0;
            dxp += dwrapped ? 10 : 0;
            mfrac = (mfrac + mxp) % 10;
            dfrac = (dfrac + dxp) % 10;
            int mpredict = predictor.treePredict(mxp / 10, magicxp);
            int dpredict = predictor.treePredict(dxp / 10, defxp);
            // System.out.println("mfrac: " + mfrac + " dfrac: " + dfrac +
            // " hit: " + hit + " mpredict: " + mpredict + " dpredict: " + dpredict +
            // " mxp(" + mwrapped + "): " + mxp + " dxp(" + dwrapped + "): " + dxp);
            // System.out.println();
            // assertEquals(mpredict, dpredict);
            assertTrue(hit + " >= " + mpredict, hit >= mpredict);
        }
        // System.out.println("*** CALIBRATED " + predictor.isAccurate(Skill.MAGIC) + "
        // " + predictor.isAccurate(Skill.DEFENCE) +" ***");

        for (int i = 0; i < iterations; i++) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            // int hit = hits[i++];
            int mxp = validMagic.get(hit);
            int dxp = validDef.get(hit);
            boolean mwrapped = ((mxp % 10) + mfrac) >= 10;
            boolean dwrapped = ((dxp % 10) + dfrac) >= 10;
            mxp += mwrapped ? 10 : 0;
            dxp += dwrapped ? 10 : 0;

            mfrac = (mfrac + mxp) % 10;
            dfrac = (dfrac + dxp) % 10;
            assertEquals(hit, predictor.treePredict(mxp / 10, magicxp));
            assertEquals(hit, predictor.treePredict(dxp / 10, defxp));
        }
    }

    @Test
    public void preciseCalcTests() {
        double scaling = 1.125d;
        Predictor.Properties properties = new Predictor.Properties(Skill.MAGIC, true, true, scaling);
        int precise = Predictor.computePrecise(23, properties);
        assertEquals(345, precise);
        precise = Predictor.computePrecise(10, properties);
        assertEquals(150, precise);
    }

    @Test
    public void preciseCursedBaboonTests() {
        double scaling = 1.15d;
        Predictor.Properties properties = new Predictor.Properties(Skill.MAGIC, false, true, scaling);
        int precise = Predictor.computePrecise(10, properties);
        assertEquals(230, precise);
    }

    @Test
    public void testPredictorDead() {
        double scaling = 1.375;
        int internalFrac = 9;
        int iterations = 1000;
        Predictor.Properties properties;
        properties = new Predictor.Properties(Skill.HITPOINTS, true, true, scaling);

        Predictor predictor = new Predictor();
        List<Integer> possibleDrops = IntStream.rangeClosed(0, iterations)
                .map(n -> Predictor.computePrecise(n, properties))
                .boxed().collect(Collectors.toList());
        internalFrac = calibrate(predictor, possibleDrops, properties, internalFrac);

        runIterations(predictor, iterations, possibleDrops, properties, internalFrac);
        predictor.reset();
        assertFalse(predictor.isDead(Skill.HITPOINTS));
        internalFrac = calibrate(predictor, possibleDrops, properties, internalFrac);
        runIterations(predictor, iterations, possibleDrops, properties, internalFrac);
        assertFalse(predictor.isDead(Skill.HITPOINTS));
    }

    @Test
    public void measureCalibrationLength() {
        final int sessions = 500;
        final int maxHit = 84;
        for (double scaling : new double[] { 1.0, 1.375, 1.495 }) {
            Predictor.Properties props = new Predictor.Properties(Skill.HITPOINTS, true, true, scaling);
            Random rng = new Random(42);
            List<Integer> possibleDrops = IntStream.rangeClosed(0, maxHit)
                    .map(n -> Predictor.computePrecise(n, props))
                    .boxed().collect(Collectors.toList());

            int[] hitsToCalibrate = new int[sessions];
            for (int s = 0; s < sessions; s++) {
                Predictor predictor = new Predictor();
                int internalFrac = rng.nextInt(10);
                int hits = 0;
                while (!predictor.isAccurate(Skill.HITPOINTS) && hits < 10_000) {
                    int hit = rng.nextInt(maxHit);
                    int xp = possibleDrops.get(hit);
                    boolean wrapped = ((xp % 10) + internalFrac) >= 10;
                    xp += wrapped ? 10 : 0;
                    internalFrac = (internalFrac + xp) % 10;
                    predictor.treePredict(xp / 10, props);
                    hits++;
                }
                hitsToCalibrate[s] = hits;
            }

            Arrays.sort(hitsToCalibrate);
            double mean = Arrays.stream(hitsToCalibrate).average().orElse(0);
            int median = hitsToCalibrate[sessions / 2];
            int p90 = hitsToCalibrate[(int) (sessions * 0.9)];
            int max = hitsToCalibrate[sessions - 1];

            System.out.printf(
                    "Calibration (n=%d, HP %.3f): mean=%.1f median=%d p90=%d max=%d%n",
                    sessions, scaling, mean, median, p90, max);

            // Regression bound — generous; tighten if numbers drift down.
            assertTrue("median calibration slow for scaling " + scaling + ": " + median,
                    median < 100);
        }
    }

    @Test
    public void measureUncalibratedAccuracy() {
        final int sessions = 500;
        final int maxHit = 84;
        for (double scaling : new double[] { 1.0, 1.375, 1.495 }) {
            Predictor.Properties props = new Predictor.Properties(Skill.HITPOINTS, true, true, scaling);
            Random rng = new Random(42);
            List<Integer> possibleDrops = IntStream.rangeClosed(0, maxHit)
                    .map(n -> Predictor.computePrecise(n, props))
                    .boxed().collect(Collectors.toList());

            int total = 0;
            int exact = 0;
            int over = 0; // predicted > actual: UNSAFE direction (would falsely indicate death)
            for (int s = 0; s < sessions; s++) {
                Predictor predictor = new Predictor();
                int internalFrac = rng.nextInt(10);
                int safety = 0;
                while (!predictor.isAccurate(Skill.HITPOINTS) && safety++ < 10_000) {
                    int hit = rng.nextInt(maxHit);
                    int xp = possibleDrops.get(hit);
                    boolean wrapped = ((xp % 10) + internalFrac) >= 10;
                    xp += wrapped ? 10 : 0;
                    internalFrac = (internalFrac + xp) % 10;
                    int predicted = predictor.treePredict(xp / 10, props);
                    total++;
                    if (predicted == hit)
                        exact++;
                    else if (predicted > hit)
                        over++;
                }
            }

            double exactPct = 100.0 * exact / total;
            double overPct = 100.0 * over / total;
            double underPct = 100.0 - exactPct - overPct;

            // exact: predicted == actual hit.
            // under: predicted < actual — safe; conservative prediction, never causes
            // false-death.
            // over: predicted > actual — unsafe; would highlight a non-fatal hit as fatal.
            System.out.printf(
                    "Uncalibrated (n=%d hits, HP %.3f): exact=%.1f%% under=%.1f%% over=%.2f%%%n",
                    total, scaling, exactPct, underPct, overPct);

            assertEquals("over-prediction at scaling " + scaling, 0, over);
        }
    }

    @Test
    public void sweepScalingsForEdgeCases() {
        final int sessions = 100;
        final int maxHit = 40;
        final int safetyCap = 200; // any session needing >200 hits is treated as "did not calibrate"
        final int startMilli = 1000;
        final int endMilli = 5000;
        final int stepMilli = 25;

        // [scaling, exactPct, meanCalib, p90Calib, overCount, neverCalibratedSessions]
        // exact: predicted == actual. under (= 100% - exact - over): predicted <
        // actual,
        // SAFE (conservative; never falsely indicates death). over: predicted > actual,
        // UNSAFE (would highlight a non-fatal hit as fatal).
        List<double[]> results = new ArrayList<>();
        int totalScalings = 0;

        for (int milli = startMilli; milli <= endMilli; milli += stepMilli) {
            double scaling = milli / 1000.0;
            totalScalings++;
            Predictor.Properties props = new Predictor.Properties(Skill.HITPOINTS, true, true, scaling);
            Random rng = new Random(42);
            List<Integer> drops = IntStream.rangeClosed(0, maxHit)
                    .map(n -> Predictor.computePrecise(n, props))
                    .boxed().collect(Collectors.toList());

            int totalPreds = 0;
            int exactPreds = 0;
            int overCount = 0;
            int neverCalibrated = 0;
            int[] hitsPerSession = new int[sessions];

            for (int s = 0; s < sessions; s++) {
                Predictor predictor = new Predictor();
                int internalFrac = rng.nextInt(10);
                int hits = 0;
                while (!predictor.isAccurate(Skill.HITPOINTS) && hits < safetyCap) {
                    int hit = rng.nextInt(maxHit);
                    int xp = drops.get(hit);
                    boolean wrapped = ((xp % 10) + internalFrac) >= 10;
                    xp += wrapped ? 10 : 0;
                    internalFrac = (internalFrac + xp) % 10;
                    int predicted = predictor.treePredict(xp / 10, props);
                    totalPreds++;
                    if (predicted == hit)
                        exactPreds++;
                    else if (predicted > hit)
                        overCount++;
                    hits++;
                }
                if (hits >= safetyCap)
                    neverCalibrated++;
                hitsPerSession[s] = hits;
            }

            Arrays.sort(hitsPerSession);
            double meanCalib = Arrays.stream(hitsPerSession).average().orElse(0);
            int p90 = hitsPerSession[(int) (sessions * 0.9)];
            double exactPct = totalPreds == 0 ? 100.0 : 100.0 * exactPreds / totalPreds;
            results.add(new double[] { scaling, exactPct, meanCalib, p90, overCount, neverCalibrated });
        }

        System.out.printf("Swept %d scalings %.3f..%.3f (step %.3f), %d sessions, safetyCap=%d%n",
                totalScalings, startMilli / 1000.0, endMilli / 1000.0, stepMilli / 1000.0, sessions, safetyCap);

        final String header = String.format("  %-9s %-8s %-8s %-10s %-6s %-8s %-6s",
                "scaling", "exact%", "under%", "meanCalib", "p90", "neverCal", "over");
        final java.util.function.Consumer<double[]> printRow = r -> {
            String neverCal = String.format("%d/%d", (int) r[5], sessions);
            System.out.printf("  %-9.3f %-8.1f %-8.1f %-10.1f %-6d %-8s %-6d%n",
                    r[0], r[1], 100.0 - r[1], r[2], (int) r[3], neverCal, (int) r[4]);
        };

        List<double[]> stuck = results.stream()
                .filter(r -> r[5] > 0)
                .sorted((a, b) -> Double.compare(b[5], a[5]))
                .collect(Collectors.toList());
        System.out.printf("Scalings with sessions that never calibrate (n=%d):%n", stuck.size());
        System.out.println(header);
        stuck.forEach(printRow);

        results.sort(Comparator.comparingDouble(a -> a[1])); // ascending exact%
        System.out.println("Worst 10 by uncalibrated exact%:");
        System.out.println(header);
        results.stream().limit(10).forEach(printRow);

        // Worst calibration time among scalings that *do* calibrate. The neverCal=100%
        // cases sit at meanCalib=safetyCap and would otherwise dominate this list.
        List<double[]> calibratable = results.stream()
                .filter(r -> r[5] == 0)
                .sorted((a, b) -> Double.compare(b[2], a[2]))
                .collect(Collectors.toList());
        System.out.println("Worst 10 by mean calibration (excluding never-calibrated):");
        System.out.println(header);
        calibratable.stream().limit(10).forEach(printRow);

        long totalOver = results.stream().mapToLong(r -> (long) r[4]).sum();
        assertEquals("over-prediction observed in sweep", 0, totalOver);
    }

    @Test
    public void testMysticsHit() {
        double scaling = 1.3d;
        Predictor.Properties properties = new Predictor.Properties(Skill.HITPOINTS, true, true, scaling);
        Predictor predictor = new Predictor();
        // precise(11) = 190 → max display with carry 9 is 19, so hit=11 cannot reach
        // display 20. Only hit=12 is consistent.
        assertEquals(12, predictor.treePredict(20, properties));
    }
}
