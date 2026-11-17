package adsa.relief.optimizer;

import adsa.relief.model.ReliefKit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dynamic Programming implementation for the 0/1 Knapsack problem in relief distribution.
 * 
 * Strategy:
 * 1. Constructs a 2D memoization table dp[n + 1][W + 1], where dp[i][w] stores the optimal
 *    priority score attainable using a subset of the first i kits with weight capacity w.
 * 2. Employs the Bellman principle of optimality:
 *      dp[i][w] = dp[i - 1][w]                                      if weight_i > w
 *      dp[i][w] = max(dp[i - 1][w], dp[i - 1][w - weight_i] + val_i) otherwise
 * 3. Backtracks from dp[n][W] to recover the exact set of relief kits selected.
 * 
 * Theoretical Analysis:
 * - Time Complexity: O(n * W) pseudo-polynomial time
 * - Space Complexity: O(n * W) table storage
 * - Optimality: Guaranteed optimal 0/1 solution for any integer capacity W.
 */
public class DynamicProgrammingOptimizer implements Optimizer {

    @Override
    public List<ReliefKit> optimize(List<ReliefKit> availableKits, int capacity) {
        if (availableKits == null || availableKits.isEmpty() || capacity <= 0) {
            return Collections.emptyList();
        }

        int n = availableKits.size();
        int W = capacity;

        // DP table: dp[i][w] holds maximum priority score using first i kits within capacity w
        int[][] dp = new int[n + 1][W + 1];

        for (int i = 1; i <= n; i++) {
            ReliefKit kit = availableKits.get(i - 1);
            int wt = kit.getWeight();
            int val = kit.getPriorityScore();

            for (int w = 0; w <= W; w++) {
                if (wt <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - wt] + val);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Backtrack to identify selected relief kits
        List<ReliefKit> selectedKits = new ArrayList<>();
        int w = W;
        for (int i = n; i >= 1; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                ReliefKit selected = availableKits.get(i - 1);
                selectedKits.add(selected);
                w -= selected.getWeight();
            }
        }

        // Reverse to maintain natural input order
        Collections.reverse(selectedKits);
        return selectedKits;
    }

    @Override
    public OptimizationAlgorithm getAlgorithmType() {
        return OptimizationAlgorithm.DYNAMIC_PROGRAMMING;
    }

    @Override
    public String getTimeComplexity() {
        return "O(n * W)";
    }

    @Override
    public String getSpaceComplexity() {
        return "O(n * W)";
    }
}
