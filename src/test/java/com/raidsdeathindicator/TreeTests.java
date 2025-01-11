package com.raidsdeathindicator;

import com.raidsdeathindicator.utils.PredictionTree;
import static org.junit.Assert.*;

import net.runelite.api.Skill;
import org.junit.Test;

import java.util.Arrays;
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
        Predictor.Properties properties = new Predictor.Properties(Skill.DEFENCE, true, true, 1.375d);
        root.insertInto(63, properties);
        List<PredictionTree> leaves = root.getLeaves();
        Integer[] expected = new Integer[]{2, 3, 4, 5, 6, 7, 8, 9};
        assertArrayEquals(expected, leaves.get(0).available.toArray());
    }

    @Test
    public void testHitFinder() {
        Predictor.Properties properties = new Predictor.Properties(Skill.DEFENCE, true, true, 1.375d);
        Predictor.Hit hit = Predictor.findHit(18, properties);
        assertEquals(13, hit.hit);
        assertTrue(hit.possibleBxp);
        assertTrue(hit.bxp);

        Predictor.Hit hit2 = Predictor.findHit(16, properties);
        assertEquals(12, hit2.hit);
        assertTrue(hit2.possibleBxp);
        assertFalse(hit2.bxp);

        Predictor.Hit hit3 = Predictor.findHit(103, properties);
        assertEquals(75, hit3.hit);
        assertFalse(hit3.possibleBxp);
        assertFalse(hit3.bxp);
    }

    @Test
    public void testPredictor() {
        double scaling = 1.21533203125d;
        // List of fixed precision integers

        final int iterations = 100;
        Predictor.Properties props = new Predictor.Properties(Skill.DEFENCE, true, true, scaling);
        runIterations(props, iterations);
    }

    private void runIterations(Predictor.Properties properties, int iterations) {
        int internalFrac = 9;
        List<Integer> possibleDrops = IntStream.rangeClosed(0, 100) // 100 is just a high number
                .map(n -> Predictor.computePrecise(n, properties))
                .boxed().collect(Collectors.toList());
        int[] n = new int[iterations];

        for (int i = 0; i < iterations; i++) {
            Predictor predictor = new Predictor();
            int count = 0;
            while (!predictor.isAccurate(Skill.DEFENCE)) {
                int idx = ThreadLocalRandom.current().nextInt(0, 84);
                //int idx = hits[i++];
                int xp = possibleDrops.get(idx);
                boolean wrapped = ((xp % 10) + internalFrac) >= 10;
                xp += wrapped ? 10 : 0;

                internalFrac = (internalFrac + xp) % 10;
                System.out.println("internal: " + internalFrac + " hit: " + idx + " xp: " + xp +" wrapped: " + wrapped);
                int predicted = predictor.treePredict(xp / 10, properties);
                count++;
            }
            n[i] = count;
            assertEquals(internalFrac, predictor.roots.get(Skill.DEFENCE).getFrac());
        }
        Arrays.sort(n);
        int sum = 0;
        for(int i = 0; i < iterations; i++) {
            sum += n[i];
        }
        System.out.println("avg: " + sum / iterations + " median: " + n[iterations / 2]);
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
        root.insertInto(85, new Predictor.Properties(Skill.DEFENCE, true, true, 1.375d));
    }

    @Test
    public void testPredictorFraction1() {
        double scaling = 1.375;
        Predictor predictor = new Predictor();
        Predictor.Properties properties = new Predictor.Properties(Skill.DEFENCE, true, true, scaling);
        int[] drops = new int[]{108, 7, 18, 35, 71, 94, 15, 86};
        for (int drop : drops) {
            predictor.treePredict(drop, properties);
        }
        assertEquals(1, predictor.roots.get(Skill.DEFENCE).getFrac());
    }
}
