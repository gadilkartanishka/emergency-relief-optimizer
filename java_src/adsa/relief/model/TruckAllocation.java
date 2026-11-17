package adsa.relief.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the allocation outcome for a single truck.
 */
public class TruckAllocation {
    private final String truckId;
    private final int capacity;
    private final List<ReliefKit> selectedKits;
    private final int totalWeight;
    private final int totalPriorityScore;

    public TruckAllocation(String truckId, int capacity, List<ReliefKit> selectedKits) {
        this.truckId = truckId;
        this.capacity = capacity;
        this.selectedKits = new ArrayList<>(selectedKits);
        
        int weightSum = 0;
        int scoreSum = 0;
        for (ReliefKit kit : selectedKits) {
            weightSum += kit.getWeight();
            scoreSum += kit.getPriorityScore();
        }
        this.totalWeight = weightSum;
        this.totalPriorityScore = scoreSum;
    }

    public String getTruckId() {
        return truckId;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<ReliefKit> getSelectedKits() {
        return Collections.unmodifiableList(selectedKits);
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public int getTotalPriorityScore() {
        return totalPriorityScore;
    }

    public int getUnusedCapacity() {
        return capacity - totalWeight;
    }

    public double getCapacityUtilization() {
        return capacity > 0 ? ((double) totalWeight / capacity) * 100.0 : 0.0;
    }

    public String getKitIdsString() {
        if (selectedKits.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedKits.size(); i++) {
            sb.append(selectedKits.get(i).getId());
            if (i < selectedKits.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
