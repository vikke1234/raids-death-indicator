package com.example;

import com.example.utils.PredictionTree;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeTests {
    @Test
    public void testLeafFinding() {
        PredictionTree root = new PredictionTree();
        root.available = new HashSet<>();
        root.available.add(8);
        root.bxp = new PredictionTree();
        root.bxp.available = new HashSet<>();
        root.bxp.available.add(9);

        root.bxp.bxp = new PredictionTree();
        root.bxp.bxp.available = new HashSet<>();
        root.bxp.bxp.available.add(1);

        root.bxp.nobxp = new PredictionTree();
        root.bxp.nobxp.available = new HashSet<>();
        root.bxp.nobxp.available.add(2);

        List<PredictionTree> leaves = PredictionTree.getLeaves(root);
        for (PredictionTree node : leaves) {
            assertTrue(PredictionTree.isLeaf(node));
            assertTrue(node.available.contains(1) || node.available.contains(2));
        }
    }

    @Test
    public void testInsertion() {
        PredictionTree root = PredictionTree.createRoot();
        root.insertInto(63, 1.375, 84);
        List<PredictionTree> leaves = root.getLeaves();
        Integer[] expected = new Integer[]{2, 3, 4, 5, 6, 7, 8, 9};
        assertArrayEquals(expected, leaves.get(0).available.toArray());
    }

    @Test
    public void testHitFinder() {
        Predictor.Hit hit = Predictor.findHit(18, 1.375d, 84);
        assertEquals(13, hit.hit);
        assertTrue(hit.possibleBxp);
        assertTrue(hit.bxp);

        Predictor.Hit hit2 = Predictor.findHit(16, 1.375d, 84);
        assertEquals(12, hit2.hit);
        assertTrue(hit2.possibleBxp);
        assertFalse(hit2.bxp);

        Predictor.Hit hit3 = Predictor.findHit(103, 1.375d, 84);
        assertEquals(75, hit3.hit);
        assertFalse(hit3.possibleBxp);
        assertFalse(hit3.bxp);
    }

    @Test
    public void testPredictor() {
        double scaling = 1.21533203125d;
        // List of fixed precision integers
        List<Integer> possibleDrops = IntStream.rangeClosed(0, 84).map(n -> (int)(n * 10 * scaling)).boxed().collect(Collectors.toList());
        int internalFrac = 9;

        final int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            Predictor predictor = new Predictor(scaling);
            while (!predictor.isAccurate()) {
                int idx = ThreadLocalRandom.current().nextInt(0, 84);
                //int idx = hits[i++];
                int xp = possibleDrops.get(idx);
                boolean wrapped = ((xp % 10) + internalFrac) >= 10;
                xp += wrapped ? 10 : 0;

                internalFrac = (internalFrac + xp) % 10;
                System.out.println("internal: " + internalFrac + " hit: " + idx + " xp: " + xp +" wrapped: " + wrapped);
                int predicted = predictor.treePredict(xp / 10);
            }
            assertEquals(internalFrac, predictor.root.getFrac());
        }
    }

    @Test
    public void testPredictorSmall() {
        PredictionTree root = new PredictionTree();
        root.nobxp = new PredictionTree();
        root.nobxp.available = new HashSet<>();
        root.nobxp.available.add(7);

        root.bxp = new PredictionTree();
        root.bxp.available = new HashSet<>();
        root.bxp.available.add(5);
        root.bxp.available.add(6);
        root.bxp.available.add(7);
        root.insertInto(85, 1.375, 84);
    }

    @Test
    public void testPredictorSmallDoNotCrash() {
        Predictor predictor = new Predictor(1.375);
        int[] values = new int[]{68, 112, 111, 49};

        for (int drop : values) {
            predictor.treePredict(drop);
        }
    }

    @Test
    public void testPredictorFraction1() {
        Predictor predictor = new Predictor(1.375);
        int[] drops = new int[]{108, 7, 18, 35, 71, 94, 15, 86};
        for (int drop : drops) {
            predictor.treePredict(drop);
        }
        assertEquals(1, predictor.root.getFrac());
    }
}
