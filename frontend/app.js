let kits = [];
let trucks = [];
let currentAlgorithm = 'DYNAMIC_PROGRAMMING';

const PRESET_SAMPLE = {
    kits: [
        { id: 'K1', destination: 'North Area', weight: 6, priorityScore: 60, quantity: 1 },
        { id: 'K2', destination: 'East Area', weight: 4, priorityScore: 50, quantity: 1 },
        { id: 'K3', destination: 'North Area', weight: 5, priorityScore: 45, quantity: 1 },
        { id: 'K4', destination: 'South Area', weight: 3, priorityScore: 25, quantity: 1 }
    ],
    trucks: [
        { id: 'T1', capacity: 10 },
        { id: 'T2', capacity: 10 }
    ]
};

function getPhysicalUnits(kitList) {
    const units = [];
    kitList.forEach(k => {
        if (k.quantity === 1) {
            units.push({ ...k });
        } else {
            for (let i = 1; i <= k.quantity; i++) {
                units.push({
                    ...k,
                    id: `${k.id}#${i}`,
                    baseId: k.id
                });
            }
        }
    });
    return units;
}

function optimizeGreedy(availableKits, capacity) {
    const sorted = [...availableKits].sort((a, b) => {
        const rA = a.priorityScore / a.weight;
        const rB = b.priorityScore / b.weight;
        if (Math.abs(rB - rA) > 1e-6) return rB - rA;
        return b.priorityScore - a.priorityScore;
    });

    const selected = [];
    let currentWeight = 0;
    for (const kit of sorted) {
        if (currentWeight + kit.weight <= capacity) {
            selected.push(kit);
            currentWeight += kit.weight;
        }
    }
    return selected;
}

function optimizeDynamicProgramming(availableKits, capacity) {
    const n = availableKits.length;
    const W = capacity;
    if (n === 0 || W <= 0) return [];

    const dp = Array.from({ length: n + 1 }, () => new Int32Array(W + 1));

    for (let i = 1; i <= n; i++) {
        const kit = availableKits[i - 1];
        const wt = kit.weight;
        const val = kit.priorityScore;

        for (let w = 0; w <= W; w++) {
            if (wt <= w) {
                const take = dp[i - 1][w - wt] + val;
                const leave = dp[i - 1][w];
                dp[i][w] = take > leave ? take : leave;
            } else {
                dp[i][w] = dp[i - 1][w];
            }
        }
    }

    const selected = [];
    let w = W;
    for (let i = n; i >= 1; i--) {
        if (dp[i][w] !== dp[i - 1][w]) {
            selected.push(availableKits[i - 1]);
            w -= availableKits[i - 1].weight;
        }
    }
    selected.reverse();
    return selected;
}

function optimizeBranchAndBound(availableKits, capacity) {
    const n = availableKits.length;
    if (n === 0 || capacity <= 0) return [];

    const sorted = [...availableKits].sort((a, b) => {
        return (b.priorityScore / b.weight) - (a.priorityScore / a.weight);
    });

    function bound(level, currentWeight, currentScore) {
        if (currentWeight >= capacity) return 0;
        let b = currentScore;
        let rem = capacity - currentWeight;
        let j = level + 1;

        while (j < n && sorted[j].weight <= rem) {
            rem -= sorted[j].weight;
            b += sorted[j].priorityScore;
            j++;
        }
        if (j < n && rem > 0) {
            b += (sorted[j].priorityScore / sorted[j].weight) * rem;
        }
        return b;
    }

    let maxScore = 0;
    let bestIndices = [];

    const queue = [{
        level: -1,
        weight: 0,
        score: 0,
        bound: bound(-1, 0, 0),
        indices: []
    }];

    while (queue.length > 0) {
        queue.sort((a, b) => b.bound - a.bound);
        const node = queue.shift();

        if (node.bound <= maxScore) continue;

        const nextLevel = node.level + 1;
        if (nextLevel >= n) continue;

        const nextKit = sorted[nextLevel];

        const inWeight = node.weight + nextKit.weight;
        const inScore = node.score + nextKit.priorityScore;
        if (inWeight <= capacity) {
            const inIndices = [...node.indices, nextLevel];
            if (inScore > maxScore) {
                maxScore = inScore;
                bestIndices = inIndices;
            }
            const inBound = bound(nextLevel, inWeight, inScore);
            if (inBound > maxScore) {
                queue.push({
                    level: nextLevel,
                    weight: inWeight,
                    score: inScore,
                    bound: inBound,
                    indices: inIndices
                });
            }
        }

        const exBound = bound(nextLevel, node.weight, node.score);
        if (exBound > maxScore) {
            queue.push({
                level: nextLevel,
                weight: node.weight,
                score: node.score,
                bound: exBound,
                indices: node.indices
            });
        }
    }

    const selected = bestIndices.map(idx => sorted[idx]);
    const selectedSet = new Set(selected);
    return availableKits.filter(k => selectedSet.has(k));
}

function runFleetAllocation(algoName) {
    const physicalUnits = getPhysicalUnits(kits);
    const pool = [...physicalUnits];
    const truckAllocations = [];

    const tStart = performance.now();

    for (const truck of trucks) {
        let assigned = [];
        if (algoName === 'GREEDY') {
            assigned = optimizeGreedy(pool, truck.capacity);
        } else if (algoName === 'DYNAMIC_PROGRAMMING') {
            assigned = optimizeDynamicProgramming(pool, truck.capacity);
        } else if (algoName === 'BRANCH_AND_BOUND') {
            assigned = optimizeBranchAndBound(pool, truck.capacity);
        }

        assigned.forEach(item => {
            const idx = pool.indexOf(item);
            if (idx > -1) pool.splice(idx, 1);
        });

        const totalWeight = assigned.reduce((sum, k) => sum + k.weight, 0);
        const totalScore = assigned.reduce((sum, k) => sum + k.priorityScore, 0);

        truckAllocations.push({
            truckId: truck.id,
            capacity: truck.capacity,
            selectedKits: assigned,
            totalWeight,
            totalScore,
            unusedCapacity: truck.capacity - totalWeight,
            utilization: truck.capacity > 0 ? (totalWeight / truck.capacity) * 100 : 0
        });
    }

    const tEnd = performance.now();

    const totalKitsAllocated = truckAllocations.reduce((sum, t) => sum + t.selectedKits.length, 0);
    const totalWeightAllocated = truckAllocations.reduce((sum, t) => sum + t.totalWeight, 0);
    const totalPriorityScore = truckAllocations.reduce((sum, t) => sum + t.totalScore, 0);
    const totalFleetCapacity = trucks.reduce((sum, t) => sum + t.capacity, 0);
    const unusedCapacity = totalFleetCapacity - totalWeightAllocated;
    const fleetUtilization = totalFleetCapacity > 0 ? (totalWeightAllocated / totalFleetCapacity) * 100 : 0;

    return {
        algoName,
        truckAllocations,
        unallocatedKits: pool,
        totalKitsAllocated,
        totalWeightAllocated,
        totalPriorityScore,
        totalFleetCapacity,
        unusedCapacity,
        fleetUtilization,
        executionTimeMs: tEnd - tStart
    };
}

function renderInventoryTable() {
    const tbody = document.getElementById('inventory-tbody');
    tbody.innerHTML = '';

    kits.forEach((kit, index) => {
        const ratio = (kit.priorityScore / kit.weight).toFixed(2);
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="mono-cell"><strong>${kit.id}</strong></td>
            <td>${kit.destination}</td>
            <td class="mono-cell">${kit.weight} kg</td>
            <td class="mono-cell">${kit.priorityScore}</td>
            <td><span class="ratio-badge">${ratio} pts/kg</span></td>
            <td class="mono-cell">${kit.quantity}</td>
            <td style="text-align: center;"><button class="btn-del" onclick="deleteKit(${index})" title="Remove kit">&times;</button></td>
        `;
        tbody.appendChild(tr);
    });

    const totalPhysical = kits.reduce((sum, k) => sum + k.quantity, 0);
    document.getElementById('inventory-badge').textContent = `${kits.length} Kits Available (${totalPhysical} Units)`;
}

function renderTrucksList() {
    const container = document.getElementById('trucks-list');
    container.innerHTML = '';

    trucks.forEach((truck, index) => {
        const div = document.createElement('div');
        div.className = 'truck-item-card';
        div.innerHTML = `
            <div class="truck-item-info">
                <span class="truck-item-name">🚛 Truck ${truck.id}</span>
                <span class="truck-item-cap">Max Payload: <strong>${truck.capacity} kg</strong></span>
            </div>
            <button class="btn-del" onclick="deleteTruck(${index})" title="Remove vehicle">&times;</button>
        `;
        container.appendChild(div);
    });

    const totalCap = trucks.reduce((sum, t) => sum + t.capacity, 0);
    document.getElementById('fleet-badge').textContent = `${trucks.length} Trucks Active (${totalCap} kg Fleet Capacity)`;
}

function renderActiveAllocation() {
    const res = runFleetAllocation(currentAlgorithm);

    document.getElementById('stat-score').textContent = res.totalPriorityScore;
    document.getElementById('stat-weight').textContent = `${res.totalWeightAllocated} kg / ${res.totalFleetCapacity} kg`;
    document.getElementById('stat-util').textContent = `${res.fleetUtilization.toFixed(1)}%`;
    document.getElementById('stat-time').textContent = `${res.executionTimeMs.toFixed(3)} ms`;

    const algoClassMap = {
        GREEDY: 'algo-greedy',
        DYNAMIC_PROGRAMMING: 'algo-dp',
        BRANCH_AND_BOUND: 'algo-bnb'
    };
    const execMeta = document.getElementById('execution-meta');
    execMeta.className = 'metrics-row ' + (algoClassMap[currentAlgorithm] || '');

    const visualContainer = document.getElementById('truck-visual-container');
    visualContainer.innerHTML = '';
    visualContainer.className = 'truck-cards-grid ' + (algoClassMap[currentAlgorithm] || '');

    res.truckAllocations.forEach(ta => {
        const card = document.createElement('div');
        card.className = 'truck-box';
        card.innerHTML = `
            <div class="truck-box-header">
                <span class="truck-box-title">🚛 Truck ${ta.truckId}</span>
                <span class="truck-box-meta">${ta.totalWeight} / ${ta.capacity} kg loaded (${ta.utilization.toFixed(0)}%)</span>
            </div>
            <div class="truck-meter">
                <div class="truck-meter-fill" style="width: ${ta.utilization}%"></div>
            </div>
            <div class="truck-kits-wrap">
                ${ta.selectedKits.length === 0 ? '<span style="font-size: 13px; color: var(--color-moss-shadow);">No relief kits allocated to this vehicle</span>' : ''}
                ${ta.selectedKits.map(k => `
                    <span class="kit-chip">
                        <strong>${k.id}</strong> (${k.weight}kg)
                        <span class="kit-chip-score">+${k.priorityScore}</span>
                    </span>
                `).join('')}
            </div>
            <div class="truck-box-footer">
                <span>Total Truck Score: <strong>${ta.totalScore} pts</strong></span>
                <span>Unused Capacity: <strong>${ta.unusedCapacity} kg</strong></span>
            </div>
        `;
        visualContainer.appendChild(card);
    });

    const unallocatedBox = document.getElementById('unallocated-container');
    if (res.unallocatedKits.length > 0) {
        const kitNames = res.unallocatedKits.map(k => `${k.id} (${k.weight}kg)`).join(', ');
        unallocatedBox.innerHTML = `
            <div class="unallocated-banner">
                <strong>Kits remaining at depot (${res.unallocatedKits.length} kits):</strong> ${kitNames}
            </div>
        `;
    } else {
        unallocatedBox.innerHTML = '';
    }

    renderComparisonTable();
}

function renderComparisonTable() {
    const algorithms = [
        { key: 'GREEDY', name: 'Greedy Algorithm', complexity: 'O(n log n)', optimality: 'Heuristic' },
        { key: 'DYNAMIC_PROGRAMMING', name: 'Dynamic Programming', complexity: 'O(n × W)', optimality: 'Optimal' },
        { key: 'BRANCH_AND_BOUND', name: 'Branch and Bound', complexity: 'O(2ⁿ) with Pruning', optimality: 'Optimal' }
    ];

    const tbody = document.getElementById('comparison-tbody');
    tbody.innerHTML = '';

    const algorithmColors = {
        GREEDY: { bg: 'rgba(217,119,6,0.06)', border: '#d97706', score: '#d97706' },
        DYNAMIC_PROGRAMMING: { bg: 'rgba(37,99,235,0.06)', border: '#2563eb', score: '#2563eb' },
        BRANCH_AND_BOUND: { bg: 'rgba(5,150,105,0.06)', border: '#059669', score: '#059669' }
    };

    algorithms.forEach(algo => {
        const res = runFleetAllocation(algo.key);
        const col = algorithmColors[algo.key] || {};
        const isActive = algo.key === currentAlgorithm;
        const tr = document.createElement('tr');
        tr.style.background = col.bg || '';
        tr.style.borderLeft = `3px solid ${col.border || 'transparent'}`;
        if (isActive) tr.style.outline = `2px solid ${col.border}`;
        tr.innerHTML = `
            <td>
                <strong style="color:${col.border}">${algo.name}</strong>
                <span class="${algo.optimality === 'Optimal' ? 'badge-optimal' : 'badge-heuristic'}" style="margin-left: 8px;">
                    ${algo.optimality}
                </span>
            </td>
            <td class="mono-cell">${algo.complexity}</td>
            <td class="mono-cell">${res.totalKitsAllocated} kits</td>
            <td class="mono-cell">${res.totalWeightAllocated} kg</td>
            <td class="mono-cell" style="font-weight: 700; color: ${col.score};">${res.totalPriorityScore}</td>
            <td class="mono-cell">${res.fleetUtilization.toFixed(1)}%</td>
            <td class="mono-cell">${res.executionTimeMs.toFixed(3)} ms</td>
        `;
        tbody.appendChild(tr);
    });
}

function deleteKit(index) {
    kits.splice(index, 1);
    renderInventoryTable();
    renderActiveAllocation();
}

function deleteTruck(index) {
    trucks.splice(index, 1);
    renderTrucksList();
    renderActiveAllocation();
}

function loadScenario(scenario) {
    kits = JSON.parse(JSON.stringify(scenario.kits));
    trucks = JSON.parse(JSON.stringify(scenario.trucks));
    renderInventoryTable();
    renderTrucksList();
    renderActiveAllocation();
}

document.addEventListener('DOMContentLoaded', () => {
    loadScenario(PRESET_SAMPLE);

    const btnSample = document.getElementById('btn-load-sample');
    if (btnSample) {
        btnSample.addEventListener('click', () => {
            loadScenario(PRESET_SAMPLE);
        });
    }

    const algoPills = document.querySelectorAll('.algo-pill');
    algoPills.forEach(btn => {
        btn.addEventListener('click', () => {
            algoPills.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentAlgorithm = btn.getAttribute('data-algo');
            renderActiveAllocation();
        });
    });

    document.getElementById('add-kit-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const id = document.getElementById('kit-id').value.trim();
        const destination = document.getElementById('kit-dest').value.trim();
        const weight = parseInt(document.getElementById('kit-weight').value);
        const priorityScore = parseInt(document.getElementById('kit-score').value);
        const quantity = parseInt(document.getElementById('kit-qty').value);

        if (!id || !destination || isNaN(weight) || isNaN(priorityScore) || isNaN(quantity)) return;

        kits.push({ id, destination, weight, priorityScore, quantity });
        e.target.reset();
        document.getElementById('kit-qty').value = '1';
        renderInventoryTable();
        renderActiveAllocation();
    });

    document.getElementById('add-truck-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const id = document.getElementById('truck-id').value.trim();
        const capacity = parseInt(document.getElementById('truck-cap').value);

        if (!id || isNaN(capacity)) return;

        trucks.push({ id, capacity });
        e.target.reset();
        renderTrucksList();
        renderActiveAllocation();
    });

    const btnRunAll = document.getElementById('btn-run-all');
    if (btnRunAll) {
        btnRunAll.addEventListener('click', () => {
            renderComparisonTable();
        });
    }

    const btnPrint = document.getElementById('btn-print-report');
    if (btnPrint) {
        btnPrint.addEventListener('click', () => {
            window.print();
        });
    }

    checkCppBackend();
});

async function checkCppBackend() {
    const badge = document.getElementById('backend-engine-badge');
    if (!badge) return;

    try {
        const response = await fetch('/api/status', { cache: 'no-store' });
        if (response.ok) {
            const data = await response.json();
            badge.textContent = `⚡ C++17 BACKEND (Active • ${data.algorithms.length} Algorithms)`;
            badge.style.background = 'rgba(34, 197, 94, 0.2)';
            badge.style.borderColor = 'rgba(34, 197, 94, 0.5)';
            return;
        }
    } catch (_) {
        try {
            const response = await fetch('http://localhost:8080/api/status', { cache: 'no-store' });
            if (response.ok) {
                const data = await response.json();
                badge.textContent = `⚡ C++17 BACKEND (Connected :8080)`;
                badge.style.background = 'rgba(34, 197, 94, 0.2)';
                badge.style.borderColor = 'rgba(34, 197, 94, 0.5)';
                return;
            }
        } catch (_) {}
    }

    badge.textContent = '⚡ C++17 BACKEND ENGINE';
}
