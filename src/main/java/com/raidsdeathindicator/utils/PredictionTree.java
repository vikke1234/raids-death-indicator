package com.raidsdeathindicator.utils;

import com.raidsdeathindicator.Predictor;
import com.google.gson.annotations.SerializedName;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Computes the internal fraction of a skill
 *
 * <p>
 * It works in the following way:
 * 1. Receive xp drop
 * 2. Look at current possible values (initially 0-9)
 * 3. Branch into once or twice, some xp drops are only possible to receive with the "extra" xp
 * 4. Adjust the filters, for example if you have the filters 0-3 and you receive 22 xp,
 *    this means that it has to be a 21.8 drop because the next hit would be 23.0. This means we
 *    can adjust the filters by 8 and remove any value that didn't wrap, so our new filters would be 8-9
 *    (previously 0-1)
 * 5. goto 1.
 * </p>
 */
public class PredictionTree {
    /**
     * For debugging purposes
     */
    @SerializedName("xp")
    public int xp;
    @SerializedName("properties")
    public Predictor.Properties properties;

    public PredictionTree nobxp;
    public PredictionTree bxp;

    /**
     * Set of active filters for the node
     */
    public Set<Integer> available;

    /**
     * Marks whether this is a valid leaf anymore
     */
    public boolean dead = false;

    /**
     * Used to create root.
     */
    public PredictionTree() { }
    public static PredictionTree createRoot() {
        PredictionTree root = new PredictionTree();
        root.available = IntStream.rangeClosed(0, 9).boxed().collect(Collectors.toSet());
        return root;
    }

    @Override
    public String toString() {
        return available.toString();
    }

    public int getFrac() {
        List<PredictionTree> leaves = getLeaves(this);
        if (leaves.size() != 1) {
            // Should always be possible to determine
            return -1;
        }

        PredictionTree leaf = leaves.get(0);
        if (leaf.available.size() != 1) {
            return -1;
        }
        return getFrac(leaf);
    }

    public static int getFrac(PredictionTree leaf) {
        assert (leaf.available.size() == 1);
        return leaf.available.stream().findFirst().get();
    }

    private PredictionTree(int xp, Predictor.Properties properties) {
        this.xp = xp;
        this.properties = properties;
    }

    private PredictionTree createBxp(Set<Integer> avail, int preciseXp, int xp, Predictor.Properties properties) {
        final int frac = preciseXp % 10;
        Set<Integer> newAvail = avail.stream().filter(n -> n + frac >= 10).map(n -> (n + frac) % 10).collect(Collectors.toSet());
        if (newAvail.isEmpty()) {
            return null;
        }
        PredictionTree node = new PredictionTree(xp, properties);
        node.available = newAvail;
        return node;
    }

    private PredictionTree createNoBxp(Set<Integer> avail, int preciseXp, int xp, Predictor.Properties properties) {
        final int frac = preciseXp % 10;
        Set<Integer> newAvail = avail.stream().filter(n -> n + frac < 10).map(n -> (n + frac) % 10).collect(Collectors.toSet());
        if (newAvail.isEmpty()) {
            return null;
        }
        PredictionTree node = new PredictionTree(xp, properties);
        node.available = newAvail;
        return node;
    }

    public void insertInto(int xp, Predictor.Properties properties) {
        if (xp == 0) {
            return;
        }
        // TODO: should this just be passed into the function?
        Predictor.Hit hit = Predictor.findHit(xp, properties);

        List<PredictionTree> leaves = getLeaves(this);

        System.out.println("XP(" + properties.skill.getName() + ", " + properties.scaling + "): " + xp + "xp hit: "+ hit.hit +
                " leaves: " + leaves.size() + " true xp(-1): " + Predictor.computePrecise(hit.hit-1, properties) / 10d +
                " true xp: " + Predictor.computePrecise(hit.hit, properties) / 10d +
                " true xp(+1): " + Predictor.computePrecise(hit.hit + 1, properties) / 10d +
                " target: " + (properties.npc != null ? properties.npc.getName() + "(idx: " + properties.npc.getIndex() + " ID: " + properties.npc.getId() + ")" : ""));
        System.out.println("---");
        if (leaves.isEmpty()) {
            System.out.println("Leaves are empty");
        }

        int precise;
        for(PredictionTree leaf : leaves) {
            Set<Integer> avail = leaf.available;
            System.out.println("Current guesses: " + avail);
            assert (!avail.isEmpty()); // should never be empty, something is wrong
            int phigh = Predictor.computePrecise(hit.hit, properties);
            int plow = Predictor.computePrecise(hit.hit-1, properties);
            int high = phigh / 10;
            int low = plow / 10;

            if(avail.size() == 1) {
                // When we get to this situation, say we have a 9 here and then
                // we receive a 20 xp drop. This can be either a 19.2 or a 20.6.
                // But now we have what we believe is the fraction, so we check;
                // lets say that the fraction is 9.
                // 20.6 + 9 = 21, we go over the drop so it has to be 19.2
                precise = Predictor.computePrecise(hit.hit, properties);
                boolean correct = (getFrac(leaf) + precise) / 10 == xp;
                if (!correct) {
                    precise = Predictor.computePrecise(hit.hit-1, properties);
                }

                if ((precise + getFrac(leaf)) / 10 != xp) {
                    leaf.dead = true;
                    System.out.println("dead leaf");
                    continue;
                }
                final int finalFrac = precise % 10;
                leaf.available = avail.stream()
                        .map(n -> (n + finalFrac) % 10)
                        .collect(Collectors.toSet());

                System.out.println("Frac: " + leaf);

                continue;
            }


            // branch on the higher hit
            if (high == xp) {
                leaf.nobxp = createNoBxp(avail, phigh, xp, properties);
                System.out.println("Creating nbxp high (" + phigh / 10d +") " + leaf.nobxp);
            } else if (high + 1 == xp) {
                leaf.bxp = createBxp(avail, phigh, xp, properties);
                System.out.println("Creating bxp high (" + phigh / 10d +") " + leaf.bxp);
            }

            // branch on the lower hit
            if (low == xp) {
                leaf.nobxp = createNoBxp(avail, plow, xp, properties);
                System.out.println("Creating nbxp low (" + plow / 10d + ") " + leaf.nobxp);
            } else if (low != 0 && low + 1 == xp) {
                leaf.bxp = createBxp(avail, plow, xp, properties);
                System.out.println("Creating bxp low (" + plow / 10d + ") " + leaf.bxp);
            }
            leaf.dead = leaf.bxp == null && leaf.nobxp == null;
        }
    }


    /**
     * Finds all the leaves of a tree using depth first search
     * @param tree root of tree
     * @return List of leaves
     */
    public static List<PredictionTree> getLeaves(PredictionTree tree) {
        List<PredictionTree> nodes = new ArrayList<>();
        Deque<PredictionTree> stack = new ArrayDeque<>();
        stack.push(tree);

        while (!stack.isEmpty()) {
            PredictionTree node = stack.pop();

            if (isLeaf(node)) {
                nodes.add(node);
                continue;
            }
            if (node.bxp != null) {
                stack.add(node.bxp);
            }
            if (node.nobxp != null) {
                stack.add(node.nobxp);
            }
        }
        return nodes;
    }

    /**
     * DFS for the tree.
     * @param root root of tree
     * @return list of nodes with no leaves
     */
    private static List<PredictionTree> findDeepestPath(PredictionTree root) {
        List<PredictionTree> deepestPath = new ArrayList<>();
        findDeepestPathHelper(root, new ArrayList<>(), deepestPath);
        return deepestPath;
    }

    private static void findDeepestPathHelper(PredictionTree node, List<PredictionTree> currentPath, List<PredictionTree> deepestPath) {
        if (node == null) return;

        currentPath.add(node);

        if (node.nobxp == null && node.bxp == null) {
            // Leaf node, check if this path is deeper
            if (currentPath.size() > deepestPath.size()) {
                deepestPath.clear();
                deepestPath.addAll(currentPath);
            }
        } else {
            // Recur for both children
            findDeepestPathHelper(node.nobxp, currentPath, deepestPath);
            findDeepestPathHelper(node.bxp, currentPath, deepestPath);
        }

        currentPath.remove(currentPath.size() - 1); // Backtrack
    }

    public List<PredictionTree> getLeaves() {
        return PredictionTree.getLeaves(this);
    }

    public static boolean isLeaf(PredictionTree tree) {
        return tree.nobxp == null && tree.bxp == null && !tree.dead;
    }
}
