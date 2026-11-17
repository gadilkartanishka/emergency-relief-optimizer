# Emergency Relief Resource Optimizer

A multi-vehicle resource allocation system for humanitarian crisis logistics, formulated as a 0/1 Multi-Knapsack problem. Implements and compares three algorithmic paradigms with a C++17 backend and an interactive web visualizer.

## Algorithms

- **Greedy Algorithm:** Local density ratio ($v_i / w_i$) ordering — $O(n \log n)$
- **Dynamic Programming:** 0/1 Knapsack optimal memoization — $O(n \cdot W)$
- **Branch and Bound:** State-space tree search with fractional relaxation upper bounding — Optimal

## Quick Start

### Prerequisites
- C++17 compiler (`clang++` or `g++`)
- `make`

### Build & Run

```bash
# Build the C++ backend
make

# Start the server (serves the web UI on http://localhost:8080)
make server
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

## Project Structure

```
adsa_mp/
├── backend/        # C++17 engine, algorithms, and REST server
│   ├── src/        # Core structs, algorithms, and server entry point
│   ├── httplib.h   # Embedded HTTP server
│   └── Makefile    # Backend build configuration
├── frontend/       # Interactive web dashboard (HTML, CSS, JS)
├── java_src/       # Reference Java implementation
└── Makefile        # Root build and run automation
```
