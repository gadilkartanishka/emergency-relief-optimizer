#include "core.hpp"
#include <algorithm>
#include <chrono>
#include <queue>
#include <numeric>

using namespace std;

int  Result::totalKits()     const { int s=0; for(auto&t:trucks) s+=t.kits.size();   return s; }
int  Result::totalWeight()   const { int s=0; for(auto&t:trucks) s+=t.totalWeight;   return s; }
int  Result::totalScore()    const { int s=0; for(auto&t:trucks) s+=t.totalScore;    return s; }
int  Result::totalCapacity() const { int s=0; for(auto&t:trucks) s+=t.capacity;      return s; }
int  Result::unusedCap()     const { return totalCapacity() - totalWeight(); }
double Result::utilization() const { return totalCapacity()>0 ? 100.0*totalWeight()/totalCapacity() : 0.0; }
bool Result::feasible()      const { for(auto&t:trucks) if(t.totalWeight>t.capacity) return false; return true; }

vector<Kit> expandKits(const vector<Kit>& kits, const vector<int>& qtys) {
    vector<Kit> out;
    for (size_t i = 0; i < kits.size(); i++) {
        int q = i < qtys.size() ? qtys[i] : 1;
        if (q == 1) { 
            out.push_back(kits[i]); 
        } else {
            for (int j = 1; j <= q; j++) {
                Kit u = kits[i];
                u.id += "#" + to_string(j);
                out.push_back(u);
            }
        }
    }
    return out;
}

vector<Kit> greedy(const vector<Kit>& pool, int capacity) {
    vector<Kit> sorted = pool;
    sort(sorted.begin(), sorted.end(), [](const Kit& a, const Kit& b) {
        return a.ratio() != b.ratio() ? a.ratio() > b.ratio() : a.score > b.score;
    });
    vector<Kit> chosen;
    int used = 0;
    for (auto& k : sorted) {
        if (used + k.weight <= capacity) { 
            chosen.push_back(k); 
            used += k.weight; 
        }
    }
    return chosen;
}

vector<Kit> dp(const vector<Kit>& pool, int W) {
    int n = pool.size();
    if (n == 0 || W <= 0) return {};

    vector<vector<int>> tbl(n+1, vector<int>(W+1, 0));
    for (int i = 1; i <= n; i++) {
        for (int w = 0; w <= W; w++) {
            tbl[i][w] = pool[i-1].weight <= w
                ? max(tbl[i-1][w], tbl[i-1][w-pool[i-1].weight] + pool[i-1].score)
                : tbl[i-1][w];
        }
    }

    vector<Kit> chosen;
    int w = W;
    for (int i = n; i >= 1; i--) {
        if (tbl[i][w] != tbl[i-1][w]) { 
            chosen.push_back(pool[i-1]); 
            w -= pool[i-1].weight; 
        }
    }
    reverse(chosen.begin(), chosen.end());
    return chosen;
}

vector<Kit> bnb(const vector<Kit>& pool, int capacity) {
    int n = pool.size();
    if (n == 0 || capacity <= 0) return {};

    vector<int> idx(n); 
    iota(idx.begin(), idx.end(), 0);
    sort(idx.begin(), idx.end(), [&](int a, int b){ return pool[a].ratio() > pool[b].ratio(); });
    vector<Kit> sorted(n);
    for (int i = 0; i < n; i++) sorted[i] = pool[idx[i]];

    struct Node {
        int level, weight, score;
        double bound;
        vector<bool> chosen;
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

    priority_queue<Node> pq;
    pq.push({-1, 0, 0, calcBound(-1, 0, 0), vector<bool>(n, false)});

    int best = 0;
    vector<bool> bestSel(n, false);

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

    vector<bool> origSel(n, false);
    for (int i = 0; i < n; i++) {
        if (bestSel[i]) origSel[idx[i]] = true;
    }

    vector<Kit> chosen;
    for (int i = 0; i < n; i++) {
        if (origSel[i]) chosen.push_back(pool[i]);
    }
    return chosen;
}

Result allocateFleet(const vector<Kit>& kits, const vector<Truck>& trucks,
                     const string& algorithm) {
    auto solve = algorithm == "GREEDY" ? greedy
               : algorithm == "BRANCH_AND_BOUND" ? bnb
               : dp;

    auto t0 = chrono::high_resolution_clock::now();

    vector<Kit> pool = kits;
    Result res;
    res.algorithm = algorithm;

    for (auto& truck : trucks) {
        vector<Kit> picked = solve(pool, truck.capacity);

        TruckAlloc ta;
        ta.truckId = truck.id;
        ta.capacity = truck.capacity;
        for (auto& k : picked) {
            ta.kits.push_back(k);
            ta.totalWeight += k.weight;
            ta.totalScore  += k.score;
            auto it = find_if(pool.begin(), pool.end(),
                              [&](const Kit& p){ return p.id == k.id; });
            if (it != pool.end()) pool.erase(it);
        }
        res.trucks.push_back(ta);
    }

    auto t1 = chrono::high_resolution_clock::now();
    res.timeNs = chrono::duration_cast<chrono::nanoseconds>(t1 - t0).count();
    return res;
}
