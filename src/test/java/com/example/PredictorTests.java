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

    private int calibrate(Predictor predictor, List<Integer> possibleDrops, double scaling, Predictor.Properties properties, int internalFrac) {
        while (!predictor.isAccurate(properties.skill)) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            //int hit = hits[i++];
            int xp = possibleDrops.get(hit);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            System.out.println("internal: " + internalFrac + " hit: " + hit + " xp: " + xp +" wrapped: " + wrapped);
            // We don't care about uncalibrated hits
            int predicted = predictor.treePredict(xp / 10, scaling, properties);
            // Make sure that when calibrating, we won't estimate higher
            assertTrue(predicted <= hit);
        }
        return internalFrac;
    }

    private void runIterations(Predictor predictor, int iterations, List<Integer> possibleDrops, double scaling, Predictor.Properties properties, int internalFrac) {
        for (int i = 0; i < iterations; i++) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            int xp = possibleDrops.get(hit);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            int predicted = predictor.treePredict(xp / 10, scaling, properties);
            System.out.println("internal: " + internalFrac + " hit(" + properties.skill.getName() + "): " + hit + " predicted: " + predicted + " xp: " + xp +" wrapped: " + wrapped);
            System.out.println();
            assertEquals(hit, predicted);
        }
    }

    private void runTest(Predictor.Properties properties, int iterations, double scaling, int internalFrac) {
        Predictor predictor = new Predictor(scaling);
        List<Integer> possibleDrops = IntStream.rangeClosed(0, 100)
                .map(n -> Predictor.computePrecise(n, scaling, properties))
                .boxed().collect(Collectors.toList());
        internalFrac = calibrate(predictor, possibleDrops, scaling, properties, internalFrac);

        runIterations(predictor, iterations, possibleDrops, scaling, properties, internalFrac);
    }
    @Test
    public void testPredictor() {
        double scaling = 1.21533203125d;
        int internalFrac = 9;
        int iterations = 1000;
        Predictor.Properties properties;
        properties = new Predictor.Properties(Skill.DEFENCE, true, true);
        runTest(properties, iterations, scaling, internalFrac);
        properties = new Predictor.Properties(Skill.STRENGTH, false, false);
        runTest(properties, iterations, scaling, internalFrac);
        properties = new Predictor.Properties(Skill.ATTACK, false, false);
        runTest(properties, iterations, scaling, internalFrac);
        properties = new Predictor.Properties(Skill.MAGIC, false, true);
        runTest(properties, iterations, scaling, internalFrac);
    }

    @Test
    public void testXpCalc() {
        double scaling = 1.375;
        int hit = 10;
        Predictor.Properties properties = new Predictor.Properties(Skill.MAGIC, true, true);
        assertEquals(182, Predictor.computePrecise(hit, scaling, properties));
        properties.skill = Skill.DEFENCE;
        assertEquals(137, Predictor.computePrecise(hit, scaling, properties));
    }

    @Test
    public void testPredictorMultipleStyles() {
        double scaling = 1.21533203125d;
        int mfrac = 9;
        int dfrac = 3;
        int iterations = 1000;
        Predictor predictor = new Predictor();

        Predictor.Properties magicxp = new Predictor.Properties(Skill.MAGIC, true, true);
        Predictor.Properties defxp = new Predictor.Properties(Skill.DEFENCE, true, true);
        List<Integer> validMagic = IntStream.rangeClosed(0, 100)
                .map(hit -> Predictor.computePrecise(hit, scaling, magicxp))
                .boxed()
                .collect(Collectors.toList());
        List<Integer> validDef = IntStream.rangeClosed(0, 100)
                .map(hit -> Predictor.computePrecise(hit, scaling, defxp))
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
            int mpredict = predictor.treePredict(mxp / 10, scaling, magicxp);
            int dpredict = predictor.treePredict(dxp / 10, scaling, defxp);
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
            assertEquals(hit, predictor.treePredict(mxp / 10, scaling, magicxp));
            assertEquals(hit, predictor.treePredict(dxp / 10, scaling, defxp));
        }
    }
}
