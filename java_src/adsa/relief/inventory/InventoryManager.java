package adsa.relief.inventory;

import adsa.relief.model.ReliefKit;

import java.util.*;

/**
 * Manages the inventory of relief kits available at the disaster relief distribution center.
 * Ensures kits are tracked with their quantities and prevents duplicate physical allocation
 * of the same relief kit across trucks.
 */
public class InventoryManager {
    private final Map<String, ReliefKit> kits;

    public InventoryManager() {
        this.kits = new LinkedHashMap<>();
    }

    /**
     * Registers a new relief kit or updates existing kit with quantity.
     */
    public void registerKit(ReliefKit kit) {
        if (kit == null) {
            throw new IllegalArgumentException("ReliefKit cannot be null.");
        }
        kits.put(kit.getId(), kit);
    }

    public void addKit(String id, String destination, int weight, int priorityScore, int quantity) {
        registerKit(new ReliefKit(id, destination, weight, priorityScore, quantity));
    }

    public ReliefKit getKit(String id) {
        return kits.get(id);
    }

    public boolean containsKit(String id) {
        return kits.containsKey(id);
    }

    public List<ReliefKit> getAllKits() {
        return new ArrayList<>(kits.values());
    }

    /**
     * Expands multi-quantity kits into a list of distinct single physical units.
     * E.g., if kit K1 has quantity 2, it produces two distinct unit instances K1#1 and K1#2.
     * If all kits have quantity 1, it maintains the original kit IDs.
     */
    public List<ReliefKit> getPhysicalKitsList() {
        List<ReliefKit> physicalUnits = new ArrayList<>();
        for (ReliefKit kit : kits.values()) {
            if (kit.getQuantity() <= 0) continue;
            if (kit.getQuantity() == 1) {
                physicalUnits.add(new ReliefKit(kit.getId(), kit.getDestination(), kit.getWeight(), kit.getPriorityScore(), 1));
            } else {
                for (int i = 1; i <= kit.getQuantity(); i++) {
                    String unitId = String.format("%s#%d", kit.getId(), i);
                    physicalUnits.add(new ReliefKit(unitId, kit.getDestination(), kit.getWeight(), kit.getPriorityScore(), 1));
                }
            }
        }
        return physicalUnits;
    }

    /**
     * Clears all registered kits.
     */
    public void clear() {
        kits.clear();
    }

    public int size() {
        return kits.size();
    }

    public int getTotalAvailableQuantity() {
        return kits.values().stream().mapToInt(ReliefKit::getQuantity).sum();
    }

    public int getTotalInventoryWeight() {
        return kits.values().stream().mapToInt(k -> k.getWeight() * k.getQuantity()).sum();
    }

    public int getTotalPriorityScore() {
        return kits.values().stream().mapToInt(k -> k.getPriorityScore() * k.getQuantity()).sum();
    }

    /**
     * Creates a deep-copy clone of this inventory manager for sandbox optimization runs.
     */
    public InventoryManager cloneSnapshot() {
        InventoryManager clone = new InventoryManager();
        for (ReliefKit k : kits.values()) {
            clone.registerKit(new ReliefKit(k.getId(), k.getDestination(), k.getWeight(), k.getPriorityScore(), k.getQuantity()));
        }
        return clone;
    }
}
