package adsa.relief.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the overall result of the fleet allocation process.
 * Contains individual truck allocations, global metrics, and algorithm metadata.
 */
public class AllocationResult {
    private final String algorithmName;
    private final List<TruckAllocation> truckAllocations;
    private final List<ReliefKit> unallocatedKits;
    private final long executionTimeNanos;
    private final int totalKitsAllocated;
    private final int totalWeightAllocated;
    private final int totalPriorityScore;
    private final int totalFleetCapacity;
    private final int unusedCapacity;
    private final double fleetCapacityUtilization;
    private final boolean feasible;

    public AllocationResult(String algorithmName,
                            List<TruckAllocation> truckAllocations,
                            List<ReliefKit> unallocatedKits,
                            long executionTimeNanos) {
        this.algorithmName = algorithmName;
        this.truckAllocations = new ArrayList<>(truckAllocations);
        this.unallocatedKits = new ArrayList<>(unallocatedKits);
        this.executionTimeNanos = executionTimeNanos;

        int kitsSum = 0;
        int weightSum = 0;
        int scoreSum = 0;
        int capacitySum = 0;
        boolean allFeasible = true;

        for (TruckAllocation ta : truckAllocations) {
            kitsSum += ta.getSelectedKits().size();
            weightSum += ta.getTotalWeight();
            scoreSum += ta.getTotalPriorityScore();
            capacitySum += ta.getCapacity();
            if (ta.getTotalWeight() > ta.getCapacity()) {
                allFeasible = false;
            }
        }

        this.totalKitsAllocated = kitsSum;
        this.totalWeightAllocated = weightSum;
        this.totalPriorityScore = scoreSum;
        this.totalFleetCapacity = capacitySum;
        this.unusedCapacity = capacitySum - weightSum;
        this.fleetCapacityUtilization = capacitySum > 0 ? ((double) weightSum / capacitySum) * 100.0 : 0.0;
        this.feasible = allFeasible && (unusedCapacity >= 0);
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<TruckAllocation> getTruckAllocations() {
        return Collections.unmodifiableList(truckAllocations);
    }

    public List<ReliefKit> getUnallocatedKits() {
        return Collections.unmodifiableList(unallocatedKits);
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    public double getExecutionTimeMillis() {
        return executionTimeNanos / 1_000_000.0;
    }

    public int getTotalKitsAllocated() {
        return totalKitsAllocated;
    }

    public int getTotalWeightAllocated() {
        return totalWeightAllocated;
    }

    public int getTotalPriorityScore() {
        return totalPriorityScore;
    }

    public int getTotalFleetCapacity() {
        return totalFleetCapacity;
    }

    public int getUnusedCapacity() {
        return unusedCapacity;
    }

    public double getFleetCapacityUtilization() {
        return fleetCapacityUtilization;
    }

    public boolean isFeasible() {
        return feasible;
    }

    /**
     * Formats the output matching Section 10 of the project report.
     */
    public String toReportString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("========================================================================\n"));
        sb.append(String.format("          ALLOCATION RESULTS: %s\n", algorithmName.toUpperCase()));
        sb.append(String.format("========================================================================\n"));
        sb.append(String.format("%-10s %-25s %-15s %-15s\n", "Truck", "Selected Kits", "Total Weight", "Priority Score"));
        sb.append("------------------------------------------------------------------------\n");

        for (TruckAllocation ta : truckAllocations) {
            sb.append(String.format("%-10s %-25s %-15s %-15d\n",
                    ta.getTruckId(),
                    ta.getKitIdsString(),
                    ta.getTotalWeight() + " kg",
                    ta.getTotalPriorityScore()));
        }

        sb.append("------------------------------------------------------------------------\n");
        sb.append("Summary:\n");
        sb.append(String.format("  Total kits allocated:        %d\n", totalKitsAllocated));
        sb.append(String.format("  Total weight allocated:      %d kg\n", totalWeightAllocated));
        sb.append(String.format("  Total priority score:        %d\n", totalPriorityScore));
        sb.append(String.format("  Total fleet capacity:        %d kg\n", totalFleetCapacity));
        sb.append(String.format("  Unused capacity:             %d kg\n", unusedCapacity));
        sb.append(String.format("  Fleet capacity utilization:  %.1f%%\n", fleetCapacityUtilization));
        sb.append(String.format("  Execution Time:              %.3f ms (%d ns)\n", getExecutionTimeMillis(), executionTimeNanos));
        sb.append(String.format("  Feasibility Status:          %s\n", feasible ? "FEASIBLE (No capacity exceeded)" : "VIOLATION"));
        sb.append("========================================================================\n");
        return sb.toString();
    }
}
