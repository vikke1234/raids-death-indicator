package com.example;

import net.runelite.api.Skill;
import org.junit.Test;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PredictorTests {

    private int calibrate(Predictor predictor, List<Integer> possibleDrops, Predictor.Properties properties, int internalFrac) {
        while (!predictor.isAccurate(properties.skill)) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            //int hit = hits[i++];
            int xp = possibleDrops.get(hit);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            System.out.println("internal: " + internalFrac + " hit: " + hit + " xp: " + xp +" wrapped: " + wrapped);
            // We don't care about uncalibrated hits
            int predicted = predictor.treePredict(xp / 10, properties);
            // Make sure that when calibrating, we won't estimate higher
            assertTrue(predicted <= hit);
        }
        return internalFrac;
    }

    private void runIterations(Predictor predictor, int iterations, List<Integer> possibleDrops, Predictor.Properties properties, int internalFrac) {
        for (int i = 0; i < iterations; i++) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            int xp = possibleDrops.get(hit);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            int predicted = predictor.treePredict(xp / 10, properties);
            System.out.println("internal: " + internalFrac + " hit(" + properties.skill.getName() + "): " + hit + " predicted: " + predicted + " xp: " + xp +" wrapped: " + wrapped);
            System.out.println();
            assertEquals(hit, predicted);
        }
    }

    private void runTest(Predictor.Properties properties, int iterations, int internalFrac) {
        Predictor predictor = new Predictor();
        List<Integer> possibleDrops = IntStream.rangeClosed(0, 100)
                .map(n -> Predictor.computePrecise(n, properties))
                .boxed().collect(Collectors.toList());
        internalFrac = calibrate(predictor, possibleDrops, properties, internalFrac);

        runIterations(predictor, iterations, possibleDrops, properties, internalFrac);
    }
    @Test
    public void testPredictor() {
        double scaling = 1.21533203125d;
        int internalFrac = 9;
        int iterations = 1000;
        Predictor.Properties properties;
        properties = new Predictor.Properties(Skill.DEFENCE, true, true, scaling);
        runTest(properties, iterations, internalFrac);
        properties = new Predictor.Properties(Skill.STRENGTH, false, false, scaling);
        runTest(properties, iterations, internalFrac);
        properties = new Predictor.Properties(Skill.ATTACK, false, false, scaling);
        runTest(properties, iterations, internalFrac);
        properties = new Predictor.Properties(Skill.MAGIC, false, true, scaling);
        runTest(properties, iterations, internalFrac);
    }

    @Test
    public void testXpCalc() {
        double scaling = 1.375d;
        int hit = 10;
        Predictor.Properties properties = new Predictor.Properties(Skill.MAGIC, true, true, scaling);
        assertEquals(183, Predictor.computePrecise(hit, properties));
        properties.skill = Skill.DEFENCE;
        assertEquals(137, Predictor.computePrecise(hit, properties));
    }

    @Test
    public void testPredictorMultipleStyles() {
        double scaling = 1.21533203125d;
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
            //int hit = hits[i++];
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
            System.out.println("mfrac: " + mfrac + " dfrac: " + dfrac +
                    " hit: " + hit + " mpredict: " + mpredict + " dpredict: " + dpredict +
                    " mxp(" + mwrapped + "): " + mxp + " dxp(" + dwrapped + "): " + dxp);
            System.out.println();
            //assertEquals(mpredict, dpredict);
            assertTrue(hit >= mpredict);
        }
        System.out.println("*** CALIBRATED " + predictor.isAccurate(Skill.MAGIC) + " " + predictor.isAccurate(Skill.DEFENCE) +" ***");
        assertEquals(mfrac, predictor.roots.get(Skill.MAGIC).getFrac());
        assertEquals(dfrac, predictor.roots.get(Skill.DEFENCE).getFrac());

        for (int i = 0; i < iterations; i++) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            //int hit = hits[i++];
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
    public void predictor1() {
        Predictor predictor = new Predictor();

    }
}
