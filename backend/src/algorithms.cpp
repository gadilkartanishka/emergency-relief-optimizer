#include "core.hpp"
#include <algorithm>
#include <chrono>
#include <queue>
#include <numeric>

int  Result::totalKits()     const { int s=0; for(auto&t:trucks) s+=t.kits.size();   return s; }
int  Result::totalWeight()   const { int s=0; for(auto&t:trucks) s+=t.totalWeight;   return s; }
int  Result::totalScore()    const { int s=0; for(auto&t:trucks) s+=t.totalScore;    return s; }
int  Result::totalCapacity() const { int s=0; for(auto&t:trucks) s+=t.capacity;      return s; }
int  Result::unusedCap()     const { return totalCapacity() - totalWeight(); }
double Result::utilization() const { return totalCapacity()>0 ? 100.0*totalWeight()/totalCapacity() : 0.0; }
bool Result::feasible()      const { for(auto&t:trucks) if(t.totalWeight>t.capacity) return false; return true; }

std::vector<Kit> expandKits(const std::vector<Kit>& kits, const std::vector<int>& qtys) {
    std::vector<Kit> out;
    for (size_t i = 0; i < kits.size(); i++) {
        int q = i < qtys.size() ? qtys[i] : 1;
        if (q == 1) { 
            out.push_back(kits[i]); 
        } else {
            for (int j = 1; j <= q; j++) {
                Kit u = kits[i];
                u.id += "#" + std::to_string(j);
                out.push_back(u);
            }
        }
    }
    return out;
}

std::vector<Kit> greedy(const std::vector<Kit>& pool, int capacity) {
    std::vector<Kit> sorted = pool;
    std::sort(sorted.begin(), sorted.end(), [](const Kit& a, const Kit& b) {
        return a.ratio() != b.ratio() ? a.ratio() > b.ratio() : a.score > b.score;
    });
    std::vector<Kit> chosen;
    int used = 0;
    for (auto& k : sorted) {
        if (used + k.weight <= capacity) { 
            chosen.push_back(k); 
            used += k.weight; 
        }
    }
    return chosen;
}

std::vector<Kit> dp(const std::vector<Kit>& pool, int W) {
    int n = pool.size();
    if (n == 0 || W <= 0) return {};

    std::vector<std::vector<int>> tbl(n+1, std::vector<int>(W+1, 0));
    for (int i = 1; i <= n; i++) {
        for (int w = 0; w <= W; w++) {
            tbl[i][w] = pool[i-1].weight <= w
                ? std::max(tbl[i-1][w], tbl[i-1][w-pool[i-1].weight] + pool[i-1].score)
                : tbl[i-1][w];
        }
    }

    std::vector<Kit> chosen;
    int w = W;
    for (int i = n; i >= 1; i--) {
        if (tbl[i][w] != tbl[i-1][w]) { 
            chosen.push_back(pool[i-1]); 
            w -= pool[i-1].weight; 
        }
    }
    std::reverse(chosen.begin(), chosen.end());
    return chosen;
}

std::vector<Kit> bnb(const std::vector<Kit>& pool, int capacity) {
    int n = pool.size();
    if (n == 0 || capacity <= 0) return {};

    std::vector<int> idx(n); 
    std::iota(idx.begin(), idx.end(), 0);
    std::sort(idx.begin(), idx.end(), [&](int a, int b){ return pool[a].ratio() > pool[b].ratio(); });
    std::vector<Kit> sorted(n);
    for (int i = 0; i < n; i++) sorted[i] = pool[idx[i]];

    struct Node {
        int level, weight, score;
        double bound;
        std::vector<bool> chosen;
        bool operator<(const Node& o) const { return bound < o.bound; }
    };

    auto calcBound = [&](int lvl, int wt, int sc) -> double {
        if (wt >= capacity) return 0;
        double b = sc, rem = capacity - wt;
        for (int j = lvl+1; j < n && sorted[j].weight <= rem; j++) {
            rem -= sorted[j].weight; 
            b += sorted[j].score;
        }
        if (lvl+1+(int)(capacity-wt-rem)/1 < n) {
            int j = lvl+1; 
            while(j<n && sorted[j].weight<=rem) { 
                rem-=sorted[j].weight; 
                j++; 
            }
            if (j < n) b += (double)sorted[j].score / sorted[j].weight * rem;
        }
        return b;
    };

    std::priority_queue<Node> pq;
    pq.push({-1, 0, 0, calcBound(-1, 0, 0), std::vector<bool>(n, false)});

    int best = 0;
    std::vector<bool> bestSel(n, false);

    while (!pq.empty()) {
        Node cur = pq.top(); 
        pq.pop();
        if (cur.bound <= best) continue;
        int next = cur.level + 1;
        if (next >= n) continue;

        if (cur.weight + sorted[next].weight <= capacity) {
            Node inc = cur;
            inc.level = next;
            inc.weight += sorted[next].weight;
            inc.score  += sorted[next].score;
            inc.chosen[next] = true;
            if (inc.score > best) { 
                best = inc.score; 
                bestSel = inc.chosen; 
            }
            inc.bound = calcBound(next, inc.weight, inc.score);
            if (inc.bound > best) pq.push(inc);
        }

        Node exc = cur; 
        exc.level = next;
        exc.bound = calcBound(next, cur.weight, cur.score);
        if (exc.bound > best) pq.push(exc);
    }

    std::vector<bool> origSel(n, false);
    for (int i = 0; i < n; i++) {
        if (bestSel[i]) origSel[idx[i]] = true;
    }

    std::vector<Kit> chosen;
    for (int i = 0; i < n; i++) {
        if (origSel[i]) chosen.push_back(pool[i]);
    }
    return chosen;
}

Result allocateFleet(const std::vector<Kit>& kits, const std::vector<Truck>& trucks,
                     const std::string& algorithm) {
    auto solve = algorithm == "GREEDY" ? greedy
               : algorithm == "BRANCH_AND_BOUND" ? bnb
               : dp;

    auto t0 = std::chrono::high_resolution_clock::now();

    std::vector<Kit> pool = kits;
    Result res;
    res.algorithm = algorithm;

    for (auto& truck : trucks) {
        std::vector<Kit> picked = solve(pool, truck.capacity);

        TruckAlloc ta;
        ta.truckId = truck.id;
        ta.capacity = truck.capacity;
        for (auto& k : picked) {
            ta.kits.push_back(k);
            ta.totalWeight += k.weight;
            ta.totalScore  += k.score;
            auto it = std::find_if(pool.begin(), pool.end(),
                                   [&](const Kit& p){ return p.id == k.id; });
            if (it != pool.end()) pool.erase(it);
        }
        res.trucks.push_back(ta);
    }

    auto t1 = std::chrono::high_resolution_clock::now();
    res.timeNs = std::chrono::duration_cast<std::chrono::nanoseconds>(t1 - t0).count();
    return res;
}
