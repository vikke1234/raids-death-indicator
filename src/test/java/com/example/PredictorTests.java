package com.example;

import net.runelite.api.Skill;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class PredictorTests {

    private int calibrate(Predictor predictor, List<Integer> possibleDrops, double scaling, Predictor.Properties properties, int internalFrac) {
        while (!predictor.isAccurate()) {
            int idx = ThreadLocalRandom.current().nextInt(0, 84);
            //int idx = hits[i++];
            int xp = possibleDrops.get(idx);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            System.out.println("internal: " + internalFrac + " hit: " + idx + " xp: " + xp +" wrapped: " + wrapped);
            // We don't care about uncalibrated hits
            predictor.treePredict(xp / 10, scaling, properties);
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
        int iterations = 100;
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
}
