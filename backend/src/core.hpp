#pragma once
#include <string>
#include <vector>

struct Kit {
    std::string id, destination;
    int weight, score;
    double ratio() const { return (double)score / weight; }
};

struct Truck {
    std::string id;
    int capacity;
};

struct TruckAlloc {
    std::string truckId;
    int capacity, totalWeight = 0, totalScore = 0;
    std::vector<Kit> kits;
    int   unused()      const { return capacity - totalWeight; }
    double utilization() const { return capacity > 0 ? 100.0 * totalWeight / capacity : 0.0; }
};

struct Result {
    std::string algorithm;
    std::vector<TruckAlloc> trucks;
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

std::vector<Kit> expandKits(const std::vector<Kit>& kits, const std::vector<int>& qtys);

Result allocateFleet(const std::vector<Kit>& kits, const std::vector<Truck>& trucks,
                     const std::string& algorithm);

std::vector<Kit> greedy(const std::vector<Kit>& pool, int capacity);
std::vector<Kit> dp    (const std::vector<Kit>& pool, int capacity);
std::vector<Kit> bnb   (const std::vector<Kit>& pool, int capacity);
