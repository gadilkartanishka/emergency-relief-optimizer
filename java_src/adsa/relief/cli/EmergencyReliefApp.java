package adsa.relief.cli;

import adsa.relief.inventory.InventoryManager;
import adsa.relief.model.AllocationResult;
import adsa.relief.model.ReliefKit;
import adsa.relief.model.Truck;
import adsa.relief.model.TruckAllocation;
import adsa.relief.optimizer.FleetOptimizer;
import adsa.relief.optimizer.OptimizationAlgorithm;

import java.util.*;

/**
 * Interactive Command-Line Interface and Driver for the Emergency Relief Resource Optimizer.
 * 
 * Project: ADSA Lab Mini Project
 * Author: Tanishka Gadilkar (PRN: 24310141, Roll No: 67, TY IT-B)
 * Institution: AISSMS Institute of Information Technology, Pune
 */
public class EmergencyReliefApp {

    private final InventoryManager inventoryManager;
    private final List<Truck> trucks;
    private final FleetOptimizer fleetOptimizer;
    private final Scanner scanner;

    public EmergencyReliefApp() {
        this.inventoryManager = new InventoryManager();
        this.trucks = new ArrayList<>();
        this.fleetOptimizer = new FleetOptimizer();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        EmergencyReliefApp app = new EmergencyReliefApp();

        if (args.length > 0) {
            String command = args[0].trim().toLowerCase();
            switch (command) {
                case "--sample":
                case "-s":
                    app.runSampleReportScenario();
                    return;
                case "--benchmark":
                case "-b":
                    app.runComprehensiveBenchmark();
                    return;
                case "--extended":
                case "-e":
                    app.loadExtendedDisasterScenario();
                    app.runComparisonAllAlgorithms();
                    return;
                case "--help":
                case "-h":
                    app.printHelp();
                    return;
            }
        }

        app.runInteractiveMenu();
    }

    private void printHelp() {
        System.out.println("EMERGENCY RELIEF RESOURCE OPTIMIZER");
        System.out.println("Usage:");
        System.out.println("  java -cp bin adsa.relief.cli.EmergencyReliefApp           (Interactive Mode)");
        System.out.println("  java -cp bin adsa.relief.cli.EmergencyReliefApp --sample   (Run Report Section 10 Sample)");
        System.out.println("  java -cp bin adsa.relief.cli.EmergencyReliefApp --extended (Run Realistic Scenario)");
        System.out.println("  java -cp bin adsa.relief.cli.EmergencyReliefApp --benchmark(Run Algorithm Performance Benchmark)");
    }

    public void runInteractiveMenu() {
        printBanner();
        loadSampleReportScenarioData();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter choice (1-9): ");
            String input = "";
            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("\nExecuting sample report scenario automatically (non-interactive stream detected)...");
                    runSampleReportScenario();
                    break;
                }
                input = scanner.nextLine().trim();
            } catch (Exception e) {
                break;
            }

            switch (input) {
                case "1":
                    displayInventoryAndFleet();
                    break;
                case "2":
                    addNewReliefKit();
                    break;
                case "3":
                    addNewTruck();
                    break;
                case "4":
                    executeSingleAlgorithm(OptimizationAlgorithm.GREEDY);
                    break;
                case "5":
                    executeSingleAlgorithm(OptimizationAlgorithm.DYNAMIC_PROGRAMMING);
                    break;
                case "6":
                    executeSingleAlgorithm(OptimizationAlgorithm.BRANCH_AND_BOUND);
                    break;
                case "7":
                    runComparisonAllAlgorithms();
                    break;
                case "8":
                    loadExtendedDisasterScenario();
                    System.out.println(">> Extended disaster relief scenario loaded successfully!");
                    displayInventoryAndFleet();
                    break;
                case "9":
                case "q":
                case "exit":
                    running = false;
                    System.out.println("Exiting Emergency Relief Resource Optimizer. Stay safe!");
                    break;
                default:
                    System.out.println("Invalid choice. Please select from 1 to 9.");
            }
        }
    }

    private void printBanner() {
        System.out.println("================================================================================");
        System.out.println("                 EMERGENCY RELIEF RESOURCE OPTIMIZER");
        System.out.println("       A Greedy, Dynamic Programming & Branch-and-Bound Allocation System");
        System.out.println("       ADSA Lab Mini Project | AISSMS IOIT, Pune | Academic Year 2026-27");
        System.out.println("       Student: Tanishka Gadilkar | PRN: 24310141 | Roll No: 67 | TY IT-B");
        System.out.println("================================================================================");
    }

    private void printMenu() {
        System.out.println("\n----------------------------- MAIN MENU -----------------------------");
        System.out.println("1. View Available Inventory & Truck Fleet");
        System.out.println("2. Add / Register Relief Kit");
        System.out.println("3. Add Transport Truck");
        System.out.println("4. Run Greedy Allocation (Ratio Criterion)");
        System.out.println("5. Run Dynamic Programming Allocation (0/1 Knapsack)");
        System.out.println("6. Run Branch and Bound Allocation (Bounded Tree Search)");
        System.out.println("7. Run Comparative Analysis (All 3 Algorithms Side-by-Side)");
        System.out.println("8. Reset to Extended Disaster Relief Scenario");
        System.out.println("9. Exit");
        System.out.println("---------------------------------------------------------------------");
    }

    public void loadSampleReportScenarioData() {
        inventoryManager.clear();
        trucks.clear();

        // Sample Input from Project Report Section 10
        // Kit ID | Destination | Weight | Priority Score
        inventoryManager.addKit("K1", "North Area", 6, 60, 1);
        inventoryManager.addKit("K2", "East Area", 4, 50, 1);
        inventoryManager.addKit("K3", "North Area", 5, 45, 1);
        inventoryManager.addKit("K4", "South Area", 3, 25, 1);

        // Truck Capacities: T1: 10 kg, T2: 10 kg
        trucks.add(new Truck("T1", 10));
        trucks.add(new Truck("T2", 10));
    }

    public void loadExtendedDisasterScenario() {
        inventoryManager.clear();
        trucks.clear();

        inventoryManager.addKit("K01-TRAUMA", "Riverside Camp", 8, 95, 1);
        inventoryManager.addKit("K02-RATIONS", "Central Hub", 12, 80, 2);
        inventoryManager.addKit("K03-WATER-PUR", "Hilltop Shelter", 6, 75, 2);
        inventoryManager.addKit("K04-BABYCARE", "Riverside Camp", 4, 60, 1);
        inventoryManager.addKit("K05-SHELTER", "Hilltop Shelter", 15, 85, 1);
        inventoryManager.addKit("K06-GEN-FUEL", "Central Hub", 18, 90, 1);
        inventoryManager.addKit("K07-BLANKETS", "Riverside Camp", 5, 45, 2);
        inventoryManager.addKit("K08-SANITATION", "Hilltop Shelter", 7, 50, 1);
        inventoryManager.addKit("K09-SAT-RADIO", "Riverside Camp", 3, 70, 1);
        inventoryManager.addKit("K10-RESCUE-KIT", "Central Hub", 10, 65, 1);

        trucks.add(new Truck("T-ALPHA", 25));
        trucks.add(new Truck("T-BETA", 20));
        trucks.add(new Truck("T-GAMMA", 15));
    }

    public void displayInventoryAndFleet() {
        System.out.println("\n=========================== CURRENT RELIEF INVENTORY ===========================");
        System.out.printf("%-10s %-20s %-12s %-15s %-12s %-10s\n", "Kit ID", "Destination", "Weight (kg)", "Priority Score", "Ratio (v/w)", "Quantity");
        System.out.println("--------------------------------------------------------------------------------");
        for (ReliefKit k : inventoryManager.getAllKits()) {
            System.out.printf("%-10s %-20s %-12d %-15d %-12.2f %-10d\n",
                    k.getId(), k.getDestination(), k.getWeight(), k.getPriorityScore(), k.getRatio(), k.getQuantity());
        }
        System.out.printf("Total Kits: %d (Physical Units: %d) | Total Weight: %d kg | Total Priority Score: %d\n",
                inventoryManager.size(),
                inventoryManager.getTotalAvailableQuantity(),
                inventoryManager.getTotalInventoryWeight(),
                inventoryManager.getTotalPriorityScore());

        System.out.println("\n============================= TRANSPORT FLEET =============================");
        System.out.printf("%-15s %-20s\n", "Truck ID", "Max Payload Capacity (kg)");
        System.out.println("-------------------------------------------------------------------------");
        int totalCap = 0;
        for (Truck t : trucks) {
            System.out.printf("%-15s %-20s\n", t.getId(), t.getCapacity() + " kg");
            totalCap += t.getCapacity();
        }
        System.out.printf("Total Fleet Trucks: %d | Combined Fleet Payload: %d kg\n", trucks.size(), totalCap);
        System.out.println("=========================================================================\n");
    }

    public void addNewReliefKit() {
        try {
            System.out.print("Enter Kit ID (e.g., K5): ");
            String id = scanner.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("Kit ID cannot be empty.");
                return;
            }
            System.out.print("Enter Target Destination (e.g., West Sector): ");
            String dest = scanner.nextLine().trim();
            System.out.print("Enter Weight in kg (integer > 0): ");
            int weight = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Priority Score (integer >= 0): ");
            int score = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Available Quantity (integer >= 1): ");
            int qty = Integer.parseInt(scanner.nextLine().trim());

            inventoryManager.addKit(id, dest, weight, score, qty);
            System.out.println(">> Relief Kit " + id + " registered successfully!");
        } catch (Exception e) {
            System.out.println("Error adding relief kit: " + e.getMessage());
        }
    }

    public void addNewTruck() {
        try {
            System.out.print("Enter Truck ID (e.g., T3): ");
            String id = scanner.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("Truck ID cannot be empty.");
                return;
            }
            System.out.print("Enter Payload Capacity in kg (integer > 0): ");
            int cap = Integer.parseInt(scanner.nextLine().trim());

            trucks.add(new Truck(id, cap));
            System.out.println(">> Truck " + id + " added to fleet successfully!");
        } catch (Exception e) {
            System.out.println("Error adding truck: " + e.getMessage());
        }
    }

    public void executeSingleAlgorithm(OptimizationAlgorithm alg) {
        List<ReliefKit> physicalKits = inventoryManager.getPhysicalKitsList();
        if (physicalKits.isEmpty()) {
            System.out.println("Inventory is empty! Please add relief kits first.");
            return;
        }
        if (trucks.isEmpty()) {
            System.out.println("Truck fleet is empty! Please add trucks first.");
            return;
        }

        AllocationResult result = fleetOptimizer.allocateFleet(physicalKits, trucks, alg);
        System.out.println("\n" + result.toReportString());
    }

    public void runComparisonAllAlgorithms() {
        List<ReliefKit> physicalKits = inventoryManager.getPhysicalKitsList();
        if (physicalKits.isEmpty()) {
            System.out.println("Inventory is empty!");
            return;
        }
        if (trucks.isEmpty()) {
            System.out.println("Truck fleet is empty!");
            return;
        }

        Map<OptimizationAlgorithm, AllocationResult> results = fleetOptimizer.compareAll(physicalKits, trucks);

        System.out.println("\n==========================================================================================");
        System.out.println("                     ALGORITHM PERFORMANCE COMPARISON MATRIX");
        System.out.println("==========================================================================================");
        System.out.printf("%-24s %-12s %-14s %-16s %-16s %-12s\n",
                "Algorithm", "Kits Loaded", "Weight (kg)", "Priority Score", "Utilization", "Time (ms)");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Map.Entry<OptimizationAlgorithm, AllocationResult> entry : results.entrySet()) {
            AllocationResult res = entry.getValue();
            System.out.printf("%-24s %-12d %-14s %-16d %-16.1f %-12.4f\n",
                    entry.getKey().getDisplayName(),
                    res.getTotalKitsAllocated(),
                    res.getTotalWeightAllocated() + " kg",
                    res.getTotalPriorityScore(),
                    res.getFleetCapacityUtilization(),
                    res.getExecutionTimeMillis());
        }
        System.out.println("==========================================================================================");

        System.out.println("\nDetailed Truck Breakdown for each algorithm:");
        for (Map.Entry<OptimizationAlgorithm, AllocationResult> entry : results.entrySet()) {
            System.out.printf("\n--- [%s] ---\n", entry.getKey().getDisplayName());
            for (TruckAllocation ta : entry.getValue().getTruckAllocations()) {
                System.out.printf("  %s -> Kits: [%s] | Weight: %d/%d kg | Priority Score: %d | Rem: %d kg\n",
                        ta.getTruckId(), ta.getKitIdsString(), ta.getTotalWeight(), ta.getCapacity(),
                        ta.getTotalPriorityScore(), ta.getUnusedCapacity());
            }
        }
    }

    public void runSampleReportScenario() {
        printBanner();
        System.out.println("\n>>> EXECUTING SAMPLE SCENARIO FROM PROJECT REPORT (SECTION 10) <<<");
        loadSampleReportScenarioData();
        displayInventoryAndFleet();

        System.out.println("\nRunning Dynamic Programming (Optimal 0/1 Knapsack Allocation):");
        AllocationResult dpResult = fleetOptimizer.allocateFleet(inventoryManager.getPhysicalKitsList(), trucks, OptimizationAlgorithm.DYNAMIC_PROGRAMMING);
        System.out.println(dpResult.toReportString());

        System.out.println("\nRunning Complete Comparison on Report Sample Data:");
        runComparisonAllAlgorithms();
    }

    public void runComprehensiveBenchmark() {
        System.out.println("\n================================================================================");
        System.out.println("                 BENCHMARKING ALGORITHMIC SCALABILITY");
        System.out.println("================================================================================");
        int[] itemCounts = {10, 20, 25, 30};
        Random rng = new Random(42);

        for (int n : itemCounts) {
            List<ReliefKit> kits = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                int wt = 2 + rng.nextInt(15);
                int sc = 10 + rng.nextInt(90);
                kits.add(new ReliefKit("BK-" + i, "Sector-" + (i % 4), wt, sc, 1));
            }
            List<Truck> benchTrucks = Arrays.asList(
                    new Truck("BT1", 30),
                    new Truck("BT2", 30)
            );

            System.out.printf("\n--- Dataset Size: %d Kits | Total Trucks: 2 (Cap: 60 kg) ---\n", n);
            for (OptimizationAlgorithm alg : OptimizationAlgorithm.values()) {
                long start = System.nanoTime();
                AllocationResult res = fleetOptimizer.allocateFleet(kits, benchTrucks, alg);
                long elapsed = System.nanoTime() - start;
                System.out.printf("  %-24s -> Score: %-5d | Weight: %-3d kg | Kits: %-3d | Time: %.3f ms\n",
                        alg.getDisplayName(), res.getTotalPriorityScore(), res.getTotalWeightAllocated(),
                        res.getTotalKitsAllocated(), elapsed / 1_000_000.0);
            }
        }
        System.out.println("================================================================================\n");
    }
}
