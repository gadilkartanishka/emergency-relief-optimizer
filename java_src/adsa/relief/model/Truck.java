package adsa.relief.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a relief transport truck with a finite carrying capacity constraint (W).
 * Tracks assigned relief kits, total loaded weight, cumulative priority score,
 * and remaining available payload capacity.
 */
public class Truck {
    private final String id;
    private final int capacity; // Maximum payload in kilograms
    private final List<ReliefKit> allocatedKits;
    private int currentWeight;
    private int currentPriorityScore;

    public Truck(String id, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Truck capacity must be strictly positive: " + capacity);
        }
        this.id = id;
        this.capacity = capacity;
        this.allocatedKits = new ArrayList<>();
        this.currentWeight = 0;
        this.currentPriorityScore = 0;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<ReliefKit> getAllocatedKits() {
        return Collections.unmodifiableList(allocatedKits);
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public int getCurrentPriorityScore() {
        return currentPriorityScore;
    }

    public int getRemainingCapacity() {
        return capacity - currentWeight;
    }

    public double getCapacityUtilization() {
        return (double) currentWeight / capacity * 100.0;
    }

    /**
     * Checks if a kit can physically fit in this truck without exceeding capacity.
     */
    public boolean canFit(ReliefKit kit) {
        return kit != null && (currentWeight + kit.getWeight() <= capacity);
    }

    /**
     * Assigns a relief kit to this truck, updating weight and priority score totals.
     */
    public boolean assignKit(ReliefKit kit) {
        if (!canFit(kit)) {
            return false;
        }
        allocatedKits.add(kit);
        currentWeight += kit.getWeight();
        currentPriorityScore += kit.getPriorityScore();
        return true;
    }

    /**
     * Clears all assigned kits and resets payload metrics.
     */
    public void reset() {
        allocatedKits.clear();
        currentWeight = 0;
        currentPriorityScore = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Truck truck = (Truck) o;
        return Objects.equals(id, truck.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Truck %s [Cap: %d kg, Load: %d kg (%s kits), Score: %d, Rem: %d kg]",
                id, capacity, currentWeight, allocatedKits.size(), currentPriorityScore, getRemainingCapacity());
    }
}
