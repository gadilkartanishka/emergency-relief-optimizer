package adsa.relief.optimizer;

import adsa.relief.model.ReliefKit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Greedy Algorithm implementation for relief kit resource allocation.
 * 
 * Strategy:
 * 1. Evaluates kits using a local criterion: the priority-to-weight ratio (density = v_i / w_i).
 * 2. Sorts kits in descending order of this ratio.
 * 3. Iteratively selects each kit if its weight does not exceed the remaining truck capacity.
 * 
 * Theoretical Analysis:
 * - Time Complexity: O(n log n) for sorting + O(n) for selection = O(n log n)
 * - Space Complexity: O(n) auxiliary memory for sorting
 * - Optimality: Optimal for fractional knapsack, but heuristic for 0/1 Knapsack.
 */
public class GreedyOptimizer implements Optimizer {

    @Override
    public List<ReliefKit> optimize(List<ReliefKit> availableKits, int capacity) {
        if (availableKits == null || availableKits.isEmpty() || capacity <= 0) {
            return Collections.emptyList();
        }

        // Create copy and sort descending by ratio (priorityScore / weight)
        List<ReliefKit> sortedKits = new ArrayList<>(availableKits);
        sortedKits.sort(Comparator
                .comparingDouble(ReliefKit::getRatio)
                .reversed()
                .thenComparing(Comparator.comparingInt(ReliefKit::getPriorityScore).reversed())
                .thenComparing(ReliefKit::getId));

        List<ReliefKit> selectedKits = new ArrayList<>();
        int currentWeight = 0;

        for (ReliefKit kit : sortedKits) {
            if (currentWeight + kit.getWeight() <= capacity) {
                selectedKits.add(kit);
                currentWeight += kit.getWeight();
            }
        }

        return selectedKits;
    }

    @Override
    public OptimizationAlgorithm getAlgorithmType() {
        return OptimizationAlgorithm.GREEDY;
    }

    @Override
    public String getTimeComplexity() {
        return "O(n log n)";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(n)";
    }
}
