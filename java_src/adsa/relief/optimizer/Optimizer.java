package adsa.relief.optimizer;

import adsa.relief.model.ReliefKit;

import java.util.List;

/**
 * Common contract for 0/1 knapsack resource optimization algorithms.
 */
public interface Optimizer {
    /**
     * Solves the 0/1 Knapsack problem for a given capacity constraint using
     * available physical kits.
     * 
     * @param availableKits List of relief kits available for selection
     * @param capacity Maximum weight capacity of the truck (in kg)
     * @return List of selected relief kits that maximize priority score within capacity
     */
    List<ReliefKit> optimize(List<ReliefKit> availableKits, int capacity);

    /**
     * Returns the algorithm classification.
     */
    OptimizationAlgorithm getAlgorithmType();

    /**
     * Returns theoretical time complexity as a formatted string.
     */
    String getTimeComplexity();

    /**
     * Returns theoretical space complexity as a formatted string.
     */
    String getSpaceComplexity();
}
