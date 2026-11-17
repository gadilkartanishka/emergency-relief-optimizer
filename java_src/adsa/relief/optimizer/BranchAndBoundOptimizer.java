package adsa.relief.optimizer;

import adsa.relief.model.ReliefKit;

import java.util.*;

/**
 * Branch and Bound implementation for the 0/1 Knapsack problem in emergency relief allocation.
 * 
 * Strategy:
 * 1. Sorts items in descending order of priority-to-weight ratio (v_i / w_i).
 * 2. Explores a state-space decision tree where each node represents a subset decision
 *    (include or exclude item at the current level).
 * 3. Prunes unpromising branches using an Upper Bound derived from the Fractional Knapsack
 *    relaxation. If a node's upper bound <= current best known score, the entire branch is pruned.
 * 4. Utilizes Best-First Search with a Max-Priority Queue to explore the most promising
 *    subproblems first.
 * 
 * Theoretical Analysis:
 * - Time Complexity: Worst-case O(2^n), but in practice prunes vast regions of search space
 * - Space Complexity: O(2^n) queue storage in worst case, O(n) average
 * - Optimality: Guaranteed optimal 0/1 Knapsack solution.
 */
public class BranchAndBoundOptimizer implements Optimizer {

    /**
     * Node representation in the Branch and Bound state-space search tree.
     */
    private static class Node implements Comparable<Node> {
        int level;                  // Index of kit considered in sorted list (-1 for root)
        int currentWeight;          // Total weight accumulated so far
        int currentScore;           // Total priority score accumulated so far
        double bound;               // Upper bound on achievable score
        BitSet selectedBits;        // BitSet tracking selected kit indices

        Node(int level, int currentWeight, int currentScore, double bound, BitSet selectedBits) {
            this.level = level;
            this.currentWeight = currentWeight;
            this.currentScore = currentScore;
            this.bound = bound;
            this.selectedBits = (BitSet) selectedBits.clone();
        }

        @Override
        public int compareTo(Node other) {
            // Max-Heap: higher bound explored first (Best-First Search)
            return Double.compare(other.bound, this.bound);
        }
    }

    @Override
    public List<ReliefKit> optimize(List<ReliefKit> availableKits, int capacity) {
        if (availableKits == null || availableKits.isEmpty() || capacity <= 0) {
            return Collections.emptyList();
        }

        int n = availableKits.size();

        // Sort kits descending by ratio to facilitate fractional bound calculation
        List<ReliefKit> sortedKits = new ArrayList<>(availableKits);
        sortedKits.sort(Comparator
                .comparingDouble(ReliefKit::getRatio)
                .reversed()
                .thenComparing(Comparator.comparingInt(ReliefKit::getPriorityScore).reversed())
                .thenComparing(ReliefKit::getId));

        // Priority Queue for Best-First Search
        PriorityQueue<Node> pq = new PriorityQueue<>();

        BitSet rootBits = new BitSet(n);
        double rootBound = calculateBound(-1, 0, 0, capacity, sortedKits, n);
        Node root = new Node(-1, 0, 0, rootBound, rootBits);
        pq.add(root);

        int maxScore = 0;
        BitSet bestSelection = new BitSet(n);

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            // Pruning: if current node's upper bound is not strictly better than maxScore, prune it
            if (current.bound <= maxScore) {
                continue;
            }

            int nextLevel = current.level + 1;
            if (nextLevel >= n) {
                continue;
            }

            ReliefKit nextKit = sortedKits.get(nextLevel);

            // -------------------------------------------------------------
            // Branch 1: INCLUDE nextKit (if capacity allows)
            // -------------------------------------------------------------
            int includeWeight = current.currentWeight + nextKit.getWeight();
            int includeScore = current.currentScore + nextKit.getPriorityScore();

            if (includeWeight <= capacity) {
                BitSet includeBits = (BitSet) current.selectedBits.clone();
                includeBits.set(nextLevel);

                if (includeScore > maxScore) {
                    maxScore = includeScore;
                    bestSelection = (BitSet) includeBits.clone();
                }

                double includeBound = calculateBound(nextLevel, includeWeight, includeScore, capacity, sortedKits, n);
                if (includeBound > maxScore) {
                    pq.add(new Node(nextLevel, includeWeight, includeScore, includeBound, includeBits));
                }
            }

            // -------------------------------------------------------------
            // Branch 2: EXCLUDE nextKit
            // -------------------------------------------------------------
            double excludeBound = calculateBound(nextLevel, current.currentWeight, current.currentScore, capacity, sortedKits, n);
            if (excludeBound > maxScore) {
                pq.add(new Node(nextLevel, current.currentWeight, current.currentScore, excludeBound, current.selectedBits));
            }
        }

        // Reconstruct selected kits in original input order
        List<ReliefKit> selected = new ArrayList<>();
        Set<ReliefKit> chosenSet = new HashSet<>();
        for (int i = 0; i < n; i++) {
            if (bestSelection.get(i)) {
                chosenSet.add(sortedKits.get(i));
            }
        }

        for (ReliefKit kit : availableKits) {
            if (chosenSet.contains(kit)) {
                selected.add(kit);
                chosenSet.remove(kit); // Handle duplicate instances correctly
            }
        }

        return selected;
    }

    /**
     * Calculates the fractional knapsack upper bound for a given node.
     */
    private double calculateBound(int level, int currentWeight, int currentScore,
                                  int capacity, List<ReliefKit> sortedKits, int n) {
        if (currentWeight >= capacity) {
            return 0.0;
        }

        double bound = currentScore;
        int remWeight = capacity - currentWeight;
        int j = level + 1;

        // Greedily add remaining items until capacity is filled
        while (j < n && sortedKits.get(j).getWeight() <= remWeight) {
            remWeight -= sortedKits.get(j).getWeight();
            bound += sortedKits.get(j).getPriorityScore();
            j++;
        }

        // Add fractional part of the next item that doesn't fully fit
        if (j < n && remWeight > 0) {
            bound += sortedKits.get(j).getRatio() * remWeight;
        }

        return bound;
    }

    @Override
    public OptimizationAlgorithm getAlgorithmType() {
        return OptimizationAlgorithm.BRANCH_AND_BOUND;
    }

    @Override
    public String getTimeComplexity() {
        return "O(2^n) worst case, fast with pruning";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(2^n) queue worst case";
    }
}
