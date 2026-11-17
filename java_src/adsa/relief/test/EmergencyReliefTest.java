package adsa.relief.test;

import adsa.relief.inventory.InventoryManager;
import adsa.relief.model.AllocationResult;
import adsa.relief.model.ReliefKit;
import adsa.relief.model.Truck;
import adsa.relief.model.TruckAllocation;
import adsa.relief.optimizer.*;

import java.util.*;

/**
 * Automated Test Suite for Emergency Relief Resource Optimizer.
 * Validates algorithmic correctness, capacity constraints, inventory tracking,
 * and confirms that the Section 10 sample output exactly matches specifications.
 */
public class EmergencyReliefTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("         RUNNING AUTOMATED TEST SUITE: EMERGENCY RELIEF OPTIMIZER");
        System.out.println("================================================================================");

        testSection10SampleDatasetDP();
        testSection10SampleDatasetGreedy();
        testSection10SampleDatasetBranchAndBound();
        testCapacityConstraintNeverViolated();
        testNoDuplicatePhysicalKitsAllocated();
        testEmptyInventoryHandling();
        testOverweightKitsSkipped();
        testMultiQuantityExpansion();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("TEST SUMMARY: %d Passed | %d Failed\n", testsPassed, testsFailed);
        System.out.println("--------------------------------------------------------------------------------");

        if (testsFailed > 0) {
            System.err.println("Some tests failed!");
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY! (100% Correctness)");
        }
    }

    private static void assertTrue(String testName, boolean condition, String errorMsg) {
        if (condition) {
            System.out.printf(" [PASS] %s\n", testName);
            testsPassed++;
        } else {
            System.err.printf(" [FAIL] %s: %s\n", testName, errorMsg);
            testsFailed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if (Objects.equals(expected, actual)) {
            System.out.printf(" [PASS] %s\n", testName);
            testsPassed++;
        } else {
            System.err.printf(" [FAIL] %s: Expected <%s> but got <%s>\n", testName, expected, actual);
            testsFailed++;
        }
    }

    /**
     * Verifies that the sample data from Section 10 of the project report produces
     * the exact figures documented in the report under Dynamic Programming:
     * - T1: K1, K2 -> Total Weight: 10 kg, Priority Score: 110
     * - T2: K3, K4 -> Total Weight: 8 kg, Priority Score: 70
     * - Total kits: 4, Total weight: 18 kg, Total score: 180, Fleet Cap: 20 kg, Unused: 2 kg, Utilization: 90%
     */
    private static void testSection10SampleDatasetDP() {
        System.out.println("\n--- Test: Report Section 10 Sample Data (Dynamic Programming) ---");
        FleetOptimizer fleetOptimizer = new FleetOptimizer();

        List<ReliefKit> kits = Arrays.asList(
                new ReliefKit("K1", "North Area", 6, 60),
                new ReliefKit("K2", "East Area", 4, 50),
                new ReliefKit("K3", "North Area", 5, 45),
                new ReliefKit("K4", "South Area", 3, 25)
        );

        List<Truck> trucks = Arrays.asList(
                new Truck("T1", 10),
                new Truck("T2", 10)
        );

        AllocationResult result = fleetOptimizer.allocateFleet(kits, trucks, OptimizationAlgorithm.DYNAMIC_PROGRAMMING);

        assertEquals("Total kits allocated == 4", 4, result.getTotalKitsAllocated());
        assertEquals("Total weight allocated == 18 kg", 18, result.getTotalWeightAllocated());
        assertEquals("Total priority score == 180", 180, result.getTotalPriorityScore());
        assertEquals("Total fleet capacity == 20 kg", 20, result.getTotalFleetCapacity());
        assertEquals("Unused capacity == 2 kg", 2, result.getUnusedCapacity());
        assertTrue("Fleet capacity utilization == 90.0%", Math.abs(result.getFleetCapacityUtilization() - 90.0) < 0.001, "Expected 90%");
        assertTrue("Allocation is feasible", result.isFeasible(), "Must not exceed truck capacity");

        TruckAllocation t1 = result.getTruckAllocations().get(0);
        assertEquals("Truck T1 weight == 10 kg", 10, t1.getTotalWeight());
        assertEquals("Truck T1 priority score == 110", 110, t1.getTotalPriorityScore());

        TruckAllocation t2 = result.getTruckAllocations().get(1);
        assertEquals("Truck T2 weight == 8 kg", 8, t2.getTotalWeight());
        assertEquals("Truck T2 priority score == 70", 70, t2.getTotalPriorityScore());
    }

    private static void testSection10SampleDatasetGreedy() {
        System.out.println("\n--- Test: Report Section 10 Sample Data (Greedy Algorithm) ---");
        FleetOptimizer fleetOptimizer = new FleetOptimizer();

        List<ReliefKit> kits = Arrays.asList(
                new ReliefKit("K1", "North Area", 6, 60),
                new ReliefKit("K2", "East Area", 4, 50),
                new ReliefKit("K3", "North Area", 5, 45),
                new ReliefKit("K4", "South Area", 3, 25)
        );

        List<Truck> trucks = Arrays.asList(
                new Truck("T1", 10),
                new Truck("T2", 10)
        );

        AllocationResult result = fleetOptimizer.allocateFleet(kits, trucks, OptimizationAlgorithm.GREEDY);

        assertEquals("Greedy: Total priority score == 180", 180, result.getTotalPriorityScore());
        assertEquals("Greedy: Total weight == 18 kg", 18, result.getTotalWeightAllocated());
        assertTrue("Greedy: Feasible allocation", result.isFeasible(), "Greedy must obey capacity");
    }

    private static void testSection10SampleDatasetBranchAndBound() {
        System.out.println("\n--- Test: Report Section 10 Sample Data (Branch and Bound) ---");
        FleetOptimizer fleetOptimizer = new FleetOptimizer();

        List<ReliefKit> kits = Arrays.asList(
                new ReliefKit("K1", "North Area", 6, 60),
                new ReliefKit("K2", "East Area", 4, 50),
                new ReliefKit("K3", "North Area", 5, 45),
                new ReliefKit("K4", "South Area", 3, 25)
        );

        List<Truck> trucks = Arrays.asList(
                new Truck("T1", 10),
                new Truck("T2", 10)
        );

        AllocationResult result = fleetOptimizer.allocateFleet(kits, trucks, OptimizationAlgorithm.BRANCH_AND_BOUND);

        assertEquals("Branch & Bound: Total priority score == 180", 180, result.getTotalPriorityScore());
        assertEquals("Branch & Bound: Total weight == 18 kg", 18, result.getTotalWeightAllocated());
        assertTrue("Branch & Bound: Feasible allocation", result.isFeasible(), "B&B must obey capacity");
    }

    private static void testCapacityConstraintNeverViolated() {
        System.out.println("\n--- Test: Vehicle Capacity Constraints Never Violated ---");
        FleetOptimizer fleetOptimizer = new FleetOptimizer();

        List<ReliefKit> kits = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            kits.add(new ReliefKit("K" + i, "Sector", i * 2, i * 5));
        }

        List<Truck> trucks = Arrays.asList(
                new Truck("T1", 15),
                new Truck("T2", 25),
                new Truck("T3", 10)
        );

        for (OptimizationAlgorithm alg : OptimizationAlgorithm.values()) {
            AllocationResult res = fleetOptimizer.allocateFleet(kits, trucks, alg);
            for (TruckAllocation ta : res.getTruckAllocations()) {
                assertTrue(alg + ": Truck " + ta.getTruckId() + " weight <= capacity",
                        ta.getTotalWeight() <= ta.getCapacity(),
                        "Weight " + ta.getTotalWeight() + " exceeded capacity " + ta.getCapacity());
            }
        }
    }

    private static void testNoDuplicatePhysicalKitsAllocated() {
        System.out.println("\n--- Test: No Duplicate Physical Kit Allocation ---");
        FleetOptimizer fleetOptimizer = new FleetOptimizer();

        List<ReliefKit> kits = Arrays.asList(
                new ReliefKit("K1", "Zone A", 4, 30),
                new ReliefKit("K2", "Zone B", 5, 40),
                new ReliefKit("K3", "Zone C", 3, 25)
        );

        List<Truck> trucks = Arrays.asList(
                new Truck("T1", 8),
                new Truck("T2", 8)
        );

        for (OptimizationAlgorithm alg : OptimizationAlgorithm.values()) {
            AllocationResult res = fleetOptimizer.allocateFleet(kits, trucks, alg);
            Set<String> assignedIds = new HashSet<>();
            for (TruckAllocation ta : res.getTruckAllocations()) {
                for (ReliefKit k : ta.getSelectedKits()) {
                    assertTrue(alg + ": Kit " + k.getId() + " allocated at most once",
                            !assignedIds.contains(k.getId()),
                            "Duplicate kit allocation detected: " + k.getId());
                    assignedIds.add(k.getId());
                }
            }
        }
    }

    private static void testEmptyInventoryHandling() {
        System.out.println("\n--- Test: Empty Inventory Handling ---");
        FleetOptimizer fleetOptimizer = new FleetOptimizer();
        List<ReliefKit> emptyList = Collections.emptyList();
        List<Truck> trucks = List.of(new Truck("T1", 10));

        for (OptimizationAlgorithm alg : OptimizationAlgorithm.values()) {
            AllocationResult res = fleetOptimizer.allocateFleet(emptyList, trucks, alg);
            assertEquals(alg + ": Total kits allocated == 0", 0, res.getTotalKitsAllocated());
            assertEquals(alg + ": Total weight == 0", 0, res.getTotalWeightAllocated());
            assertEquals(alg + ": Total score == 0", 0, res.getTotalPriorityScore());
        }
    }

    private static void testOverweightKitsSkipped() {
        System.out.println("\n--- Test: Kits Exceeding Truck Capacity Skipped ---");
        FleetOptimizer fleetOptimizer = new FleetOptimizer();
        List<ReliefKit> kits = Arrays.asList(
                new ReliefKit("HUGE_KIT", "Zone", 50, 1000), // weighs 50kg
                new ReliefKit("SMALL_KIT", "Zone", 5, 40)    // weighs 5kg
        );
        List<Truck> trucks = List.of(new Truck("T1", 10)); // capacity 10kg

        for (OptimizationAlgorithm alg : OptimizationAlgorithm.values()) {
            AllocationResult res = fleetOptimizer.allocateFleet(kits, trucks, alg);
            assertEquals(alg + ": Only 1 kit allocated", 1, res.getTotalKitsAllocated());
            assertEquals(alg + ": Allocated weight is 5", 5, res.getTotalWeightAllocated());
            assertEquals(alg + ": Huge kit was not allocated", "SMALL_KIT", res.getTruckAllocations().get(0).getSelectedKits().get(0).getId());
        }
    }

    private static void testMultiQuantityExpansion() {
        System.out.println("\n--- Test: Inventory Multi-Quantity Expansion ---");
        InventoryManager im = new InventoryManager();
        im.addKit("FOOD", "Zone", 4, 30, 3); // 3 physical units
        im.addKit("MEDS", "Zone", 2, 50, 1); // 1 physical unit

        List<ReliefKit> physicalUnits = im.getPhysicalKitsList();
        assertEquals("Total physical units == 4", 4, physicalUnits.size());
        assertEquals("FOOD#1 unit exists", "FOOD#1", physicalUnits.get(0).getId());
        assertEquals("FOOD#2 unit exists", "FOOD#2", physicalUnits.get(1).getId());
        assertEquals("FOOD#3 unit exists", "FOOD#3", physicalUnits.get(2).getId());
        assertEquals("MEDS unit exists", "MEDS", physicalUnits.get(3).getId());
    }
}
