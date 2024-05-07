package com.example;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class PredictorTests {

    @Test
    public void testPredictor() {
        double scaling = 1.21533203125d;
        // List of fixed precision integers
        List<Integer> possibleDrops = IntStream.rangeClosed(0, 84).map(n -> (int)(n * 10 * scaling)).boxed().collect(Collectors.toList());
        int internalFrac = 9;

        Predictor predictor = new Predictor(scaling);
        while (!predictor.isAccurate()) {
            int idx = ThreadLocalRandom.current().nextInt(0, 84);
            //int idx = hits[i++];
            int xp = possibleDrops.get(idx);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            System.out.println("internal: " + internalFrac + " hit: " + idx + " xp: " + xp +" wrapped: " + wrapped);
            // We don't care about uncalibrated hits
            predictor.treePredict(xp / 10);
        }
        System.out.println("***Predictor calibrated***\n");

        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            int hit = ThreadLocalRandom.current().nextInt(0, 84);
            int xp = possibleDrops.get(hit);
            boolean wrapped = ((xp % 10) + internalFrac) >= 10;
            xp += wrapped ? 10 : 0;

            internalFrac = (internalFrac + xp) % 10;
            System.out.println("internal: " + internalFrac + " hit: " + hit + " xp: " + xp +" wrapped: " + wrapped);
            int predicted = predictor.treePredict(xp / 10);
            assertEquals(hit, predicted);
        }
    }
}
