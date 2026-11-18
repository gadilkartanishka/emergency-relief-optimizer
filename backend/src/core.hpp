#pragma once
#include <string>
#include <vector>

using namespace std;

struct Kit {
    string id, destination;
    int weight, score;
    double ratio() const { return (double)score / weight; }
};

struct Truck {
    string id;
    int capacity;
};

struct TruckAlloc {
    string truckId;
    int capacity, totalWeight = 0, totalScore = 0;
    vector<Kit> kits;
    int   unused()      const { return capacity - totalWeight; }
    double utilization() const { return capacity > 0 ? 100.0 * totalWeight / capacity : 0.0; }
};

struct Result {
    string algorithm;
    vector<TruckAlloc> trucks;
    long long timeNs = 0;

    int  totalKits()     const;
    int  totalWeight()   const;
    int  totalScore()    const;
    int  totalCapacity() const;
    int  unusedCap()     const;
    double utilization() const;
    bool feasible()      const;
    double timeMs()      const { return timeNs / 1e6; }
};

vector<Kit> expandKits(const vector<Kit>& kits, const vector<int>& qtys);

Result allocateFleet(const vector<Kit>& kits, const vector<Truck>& trucks,
                     const string& algorithm);

vector<Kit> greedy(const vector<Kit>& pool, int capacity);
vector<Kit> dp    (const vector<Kit>& pool, int capacity);
vector<Kit> bnb   (const vector<Kit>& pool, int capacity);
