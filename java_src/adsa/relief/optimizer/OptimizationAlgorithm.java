package adsa.relief.optimizer;

/**
 * Enumeration of the three algorithmic techniques implemented in this project.
 */
public enum OptimizationAlgorithm {
    GREEDY("Greedy Algorithm", "Selects items using a local criterion (priority-to-weight ratio). Fast O(n log n), but may be suboptimal for 0/1 knapsack."),
    DYNAMIC_PROGRAMMING("Dynamic Programming", "Solves 0/1 knapsack optimally via subproblem memoization O(n*W). Guaranteed optimal for integer capacities."),
    BRANCH_AND_BOUND("Branch and Bound", "Explores decision tree with fractional knapsack bounds for aggressive pruning. Guaranteed optimal for 0/1 formulation.");

    private final String displayName;
    private final String description;

    OptimizationAlgorithm(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
