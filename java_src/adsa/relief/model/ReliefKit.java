package adsa.relief.model;

import java.util.Objects;

/**
 * Represents an emergency relief kit containing critical supplies.
 * Each kit has an identifier, destination, weight (in kg), priority score (benefit value),
 * and an available inventory quantity.
 * 
 * In knapsack terms:
 * - Weight corresponds to item weight (w_i)
 * - Priority Score corresponds to item value (v_i)
 */
public class ReliefKit implements Comparable<ReliefKit> {
    private final String id;
    private final String destination;
    private final int weight;           // Weight in kilograms
    private final int priorityScore;    // Benefit/Urgency score (higher is more critical)
    private int quantity;               // Available physical quantity

    public ReliefKit(String id, String destination, int weight, int priorityScore, int quantity) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Kit weight must be strictly positive: " + weight);
        }
        if (priorityScore < 0) {
            throw new IllegalArgumentException("Kit priority score cannot be negative: " + priorityScore);
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Kit quantity cannot be negative: " + quantity);
        }
        this.id = id;
        this.destination = destination;
        this.weight = weight;
        this.priorityScore = priorityScore;
        this.quantity = quantity;
    }

    public ReliefKit(String id, String destination, int weight, int priorityScore) {
        this(id, destination, weight, priorityScore, 1);
    }

    public String getId() {
        return id;
    }

    public String getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        this.quantity = quantity;
    }

    /**
     * Priority to weight ratio (benefit density) used by the Greedy criterion
     * and Branch & Bound upper bounding relaxation.
     */
    public double getRatio() {
        return (double) priorityScore / weight;
    }

    @Override
    public int compareTo(ReliefKit other) {
        // Default sort descending by priority-to-weight ratio
        return Double.compare(other.getRatio(), this.getRatio());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReliefKit reliefKit = (ReliefKit) o;
        return Objects.equals(id, reliefKit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s | %s | %d kg | Score: %d | Ratio: %.2f]",
                id, destination, weight, priorityScore, getRatio());
    }
}
