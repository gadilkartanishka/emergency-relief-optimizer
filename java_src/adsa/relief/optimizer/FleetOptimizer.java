package adsa.relief.optimizer;

import adsa.relief.model.AllocationResult;
import adsa.relief.model.ReliefKit;
import adsa.relief.model.Truck;
import adsa.relief.model.TruckAllocation;

import java.util.*;

/**
 * Coordinates resource allocation across a fleet of relief trucks.
 * Ensures:
 * 1. Physical inventory constraint: No relief kit is allocated more than once.
 * 2. Capacity constraint: No truck is overloaded beyond its maximum carrying payload.
 * 3. Supports Greedy, Dynamic Programming, and Branch and Bound optimization paradigms.
 * 4. Benchmarking and execution time profiling.
 */
public class FleetOptimizer {

    private final Map<OptimizationAlgorithm, Optimizer> optimizers;

    public FleetOptimizer() {
        this.optimizers = new EnumMap<>(OptimizationAlgorithm.class);
        this.optimizers.put(OptimizationAlgorithm.GREEDY, new GreedyOptimizer());
        this.optimizers.put(OptimizationAlgorithm.DYNAMIC_PROGRAMMING, new DynamicProgrammingOptimizer());
        this.optimizers.put(OptimizationAlgorithm.BRANCH_AND_BOUND, new BranchAndBoundOptimizer());
    }

    public Optimizer getOptimizer(OptimizationAlgorithm algorithm) {
        return optimizers.get(algorithm);
    }

    /**
     * Allocates available relief kits across the truck fleet using the chosen algorithm.
     * 
     * @param kits Available physical relief kits
     * @param trucks Fleet of transport trucks with predefined capacities
     * @param algorithm Algorithm technique to use
     * @return Complete allocation result with per-truck breakdowns and fleet metrics
     */
    public AllocationResult allocateFleet(List<ReliefKit> kits, List<Truck> trucks, OptimizationAlgorithm algorithm) {
        if (kits == null || trucks == null) {
            throw new IllegalArgumentException("Kits and trucks must not be null.");
        }

        Optimizer optimizer = optimizers.get(algorithm);
        if (optimizer == null) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }

        long startTime = System.nanoTime();

        // Working pool of available kits (preventing duplicate physical allocation)
        List<ReliefKit> pool = new ArrayList<>(kits);
        List<TruckAllocation> truckAllocations = new ArrayList<>();

        for (Truck truck : trucks) {
            // Run algorithm for this truck's capacity on remaining pool of kits
            List<ReliefKit> assigned = optimizer.optimize(pool, truck.getCapacity());

            // Remove assigned physical kits from available pool
            for (ReliefKit assignedKit : assigned) {
                pool.remove(assignedKit);
            }

            truckAllocations.add(new TruckAllocation(truck.getId(), truck.getCapacity(), assigned));
        }

        long executionTime = System.nanoTime() - startTime;

        return new AllocationResult(
                algorithm.getDisplayName(),
                truckAllocations,
                pool, // remaining unallocated kits
                executionTime
        );
    }

    /**
     * Runs all three algorithms on the same dataset for comprehensive comparison.
     */
    public Map<OptimizationAlgorithm, AllocationResult> compareAll(List<ReliefKit> kits, List<Truck> trucks) {
        Map<OptimizationAlgorithm, AllocationResult> results = new LinkedHashMap<>();
        for (OptimizationAlgorithm alg : OptimizationAlgorithm.values()) {
            results.put(alg, allocateFleet(kits, trucks, alg));
        }
        return results;
    }
}
