# Emergency Relief Resource Optimizer
## A Greedy and Dynamic Programming Based Resource Allocation System (C++ Edition)

**ADSA Lab Mini Project**  
**AISSMS Institute of Information Technology, Pune**  
**Academic Year:** 2026–27  
**Student:** Tanishka Gadilkar  
**PRN:** 24310141 | **Roll No.:** 67 | **Class:** TY IT-B  
**Course:** Advanced Data Structures and Algorithms (ADSA)  

---

## 📌 Project Overview

During humanitarian crises (floods, earthquakes, landslides), relief dispatch coordinators face the challenge of distributing limited emergency supplies across disaster-hit regions with finite transport fleets.

The **Emergency Relief Resource Optimizer** formulates and solves this critical challenge as a multi-vehicle **0/1 Knapsack Resource Allocation Problem**, implementing and benchmarking three algorithmic paradigms in high-performance **C++17**:
1. **Greedy Algorithm:** Local density criterion ($v_i / w_i$) ordering with $O(n \log n)$ time.
2. **Dynamic Programming:** Bellman optimality memoization with $O(n \cdot W)$ time and guaranteed global optimum.
3. **Branch and Bound:** State-space tree search with Fractional Knapsack upper bound relaxation and Best-First Search pruning.

---

## 🚀 Quick Start

### Prerequisites
- **C++ Compiler (C++17+)**: `clang++` or `g++`
- `make` (standard POSIX build automation)

### 1. Build and Run via Makefile

```bash
# Compile all C++ sources (application and test binaries)
make

# Run automated C++ test suite (61 unit & integration tests)
make test

# Run Project Report Section 10 sample dataset
make sample

# Run extended disaster scenario (10 kits, 3 trucks)
make extended

# Run algorithmic scalability benchmark (nanosecond precision)
make benchmark

# Start the embedded C++ HTTP REST server & Web Visualizer on port 8080
make server

# Start the interactive CLI console
make run
```

### 2. Run via Bash Scripts

```bash
# Execute sample scenario
./run.sh --sample

# Execute extended scenario
./run.sh --extended

# Run test suite
./test.sh
```

---

## 🌐 Modern Web Dashboard & Embedded C++ REST Server

A visual web dashboard is included in [`web/`](web/):
- **Live Visualizer:** Displays truck loading progress bars, allocated kits, and remaining capacity.
- **Algorithm Switcher:** Instant comparison between Greedy, Dynamic Programming, and Branch and Bound.
- **Embedded C++ HTTP Server:** Running `make server` serves the web dashboard at `http://localhost:8080` backed by native C++ REST API endpoints (`/api/status`, `/api/optimize`, `/api/compare`).
- **Export Feature:** 1-click printable official project report.

To view:
```bash
# Option 1: Native C++ Web Server (Recommended)
make server
# Open http://localhost:8080 in your browser

# Option 2: Standalone file opening (with client-side fallback)
open web/index.html
```

---

## 📊 Sample Output (Report Section 10)

```
========================================================================
          ALLOCATION RESULTS: DYNAMIC PROGRAMMING
========================================================================
Truck      Selected Kits             Total Weight    Priority Score 
------------------------------------------------------------------------
T1         K1, K2                    10 kg           110            
T2         K3, K4                    8 kg            70             
------------------------------------------------------------------------
Summary:
  Total kits allocated:        4
  Total weight allocated:      18 kg
  Total priority score:        180
  Total fleet capacity:        20 kg
  Unused capacity:             2 kg
  Fleet capacity utilization:  90.0%
  Execution Time:              0.006 ms (5750 ns)
  Feasibility Status:          FEASIBLE (No capacity exceeded)
========================================================================
```

---

## 📁 Repository Structure

```
adsa_mp/
├── REPORT.md                                # Complete Academic Mini Project Report
├── Makefile                                 # High-performance C++ build and run rules
├── run.sh                                   # CLI execution helper script
├── test.sh                                  # Test suite runner script
├── include/
│   └── adsa/relief/
│       ├── model/
│       │   ├── ReliefKit.hpp                # Kit entity (ID, destination, weight, priority, quantity)
│       │   ├── Truck.hpp                    # Transport vehicle entity & capacity constraint tracker
│       │   ├── TruckAllocation.hpp          # Single truck allocation outcome encapsulation
│       │   └── AllocationResult.hpp         # Fleet allocation summary & formatted metrics
│       ├── inventory/
│       │   └── InventoryManager.hpp         # Physical inventory tracker (prevents duplication)
│       ├── optimizer/
│       │   ├── OptimizationAlgorithm.hpp    # Enum & metadata for algorithms
│       │   ├── Optimizer.hpp                # Abstract 0/1 knapsack optimizer interface
│       │   ├── GreedyOptimizer.hpp          # Ratio criterion optimizer
│       │   ├── DynamicProgrammingOptimizer.hpp # O(n*W) Bellman memoization optimizer
│       │   ├── BranchAndBoundOptimizer.hpp  # Fractional bound relaxation & Best-First Search
│       │   └── FleetOptimizer.hpp           # Multi-vehicle fleet coordinator & profiler
│       ├── server/
│       │   └── ReliefServer.hpp             # Zero-dependency embedded HTTP REST server
│       └── cli/
│           └── EmergencyReliefApp.hpp       # Interactive console application
├── src/
│   ├── optimizer/
│   │   ├── GreedyOptimizer.cpp
│   │   ├── DynamicProgrammingOptimizer.cpp
│   │   ├── BranchAndBoundOptimizer.cpp
│   │   └── FleetOptimizer.cpp
│   ├── server/
│   │   └── ReliefServer.cpp                 # POSIX socket server with REST API
│   ├── cli/
│   │   └── EmergencyReliefApp.cpp           # CLI entry point & menu driver
│   └── test/
│       └── EmergencyReliefTest.cpp          # 61 automated unit and integration tests
├── java_src/                                # Preserved original Java reference implementation
├── screenshots/
└── web/
    ├── index.html                           # Visual interactive dashboard
    ├── style.css                            # Modern styling & glassmorphism
    └── app.js                               # Dashboard logic & C++ REST API integration
```
