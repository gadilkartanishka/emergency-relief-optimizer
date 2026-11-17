#include "../httplib.h"
#include "core.hpp"
#include <iostream>
#include <sstream>
#include <string>

static std::string jstr(const std::string& s) { return "\"" + s + "\""; }

static std::string extractStr(const std::string& j, const std::string& key) {
    auto k = j.find("\"" + key + "\""); if (k == std::string::npos) return "";
    auto c = j.find(':', k); if (c == std::string::npos) return "";
    auto q = j.find('"', c); if (q == std::string::npos) return "";
    auto e = j.find('"', q+1); return e == std::string::npos ? "" : j.substr(q+1, e-q-1);
}

static int extractInt(const std::string& j, const std::string& key, int def = 0) {
    auto k = j.find("\"" + key + "\""); if (k == std::string::npos) return def;
    auto c = j.find(':', k); if (c == std::string::npos) return def;
    while (c < j.size() && (j[c]==':'||std::isspace(j[c]))) c++;
    auto e = c; while (e < j.size() && (std::isdigit(j[e])||j[e]=='-')) e++;
    if (e > c) try { return std::stoi(j.substr(c, e-c)); } catch(...) {}
    return def;
}

static std::vector<std::string> extractObjArray(const std::string& j, const std::string& key) {
    std::vector<std::string> out;
    auto k = j.find("\"" + key + "\""); if (k == std::string::npos) return out;
    auto arr = j.find('[', k); if (arr == std::string::npos) return out;
    int depth = 0; size_t start = std::string::npos;
    for (size_t i = arr+1; i < j.size(); i++) {
        if      (j[i]=='{') { if (!depth++) start = i; }
        else if (j[i]=='}') { if (!--depth && start!=std::string::npos) { out.push_back(j.substr(start,i-start+1)); start=std::string::npos; } }
        else if (j[i]==']' && !depth) break;
    }
    return out;
}

static void parseBody(const std::string& body,
                      std::vector<Kit>& kits, std::vector<int>& qtys,
                      std::vector<Truck>& trucks) {
    for (auto& o : extractObjArray(body, "kits")) {
        std::string id = extractStr(o, "id");
        if (id.empty()) continue;
        kits.push_back({id, extractStr(o,"destination"), extractInt(o,"weight",1), extractInt(o,"priorityScore",0)});
        qtys.push_back(extractInt(o, "quantity", 1));
    }
    for (auto& o : extractObjArray(body, "trucks")) {
        std::string id = extractStr(o, "id");
        if (!id.empty()) trucks.push_back({id, extractInt(o,"capacity",10)});
    }
    if (trucks.empty()) trucks.push_back({"T1", 10});
}

static std::string resultJson(const Result& r) {
    std::ostringstream o;
    o << "{"
      << jstr("algorithmName")           << ":" << jstr(r.algorithm)       << ","
      << jstr("executionTimeNanos")       << ":" << r.timeNs                 << ","
      << jstr("executionTimeMillis")      << ":" << r.timeMs()               << ","
      << jstr("totalKitsAllocated")       << ":" << r.totalKits()            << ","
      << jstr("totalWeightAllocated")     << ":" << r.totalWeight()          << ","
      << jstr("totalPriorityScore")       << ":" << r.totalScore()           << ","
      << jstr("totalFleetCapacity")       << ":" << r.totalCapacity()        << ","
      << jstr("unusedCapacity")           << ":" << r.unusedCap()            << ","
      << jstr("fleetCapacityUtilization") << ":" << r.utilization()          << ","
      << jstr("feasible")                 << ":" << (r.feasible()?"true":"false") << ","
      << jstr("truckAllocations")         << ":[";

    for (size_t i = 0; i < r.trucks.size(); i++) {
        const auto& t = r.trucks[i];
        o << "{"
          << jstr("truckId")            << ":" << jstr(t.truckId)        << ","
          << jstr("capacity")           << ":" << t.capacity              << ","
          << jstr("totalWeight")        << ":" << t.totalWeight           << ","
          << jstr("totalPriorityScore") << ":" << t.totalScore            << ","
          << jstr("unusedCapacity")     << ":" << t.unused()              << ","
          << jstr("capacityUtilization")<< ":" << t.utilization()         << ","
          << jstr("selectedKits")       << ":[";
        for (size_t j = 0; j < t.kits.size(); j++) {
            const auto& k = t.kits[j];
            o << "{" << jstr("id") << ":" << jstr(k.id)
              << "," << jstr("destination") << ":" << jstr(k.destination)
              << "," << jstr("weight") << ":" << k.weight
              << "," << jstr("priorityScore") << ":" << k.score
              << "," << jstr("ratio") << ":" << k.ratio() << "}";
            if (j+1 < t.kits.size()) o << ",";
        }
        o << "]}";
        if (i+1 < r.trucks.size()) o << ",";
    }
    o << "]}";
    return o.str();
}

int main(int argc, char* argv[]) {
    int port = 8080;
    std::string frontendDir = "../frontend";
    if (argc > 1) try { port = std::stoi(argv[1]); } catch(...) {}
    if (argc > 2) frontendDir = argv[2];

    httplib::Server svr;

    svr.set_mount_point("/", frontendDir);

    svr.Get("/api/status", [](const httplib::Request&, httplib::Response& res) {
        res.set_content(
            "{\"status\":\"online\",\"backend\":\"C++17\","
            "\"algorithms\":[\"GREEDY\",\"DYNAMIC_PROGRAMMING\",\"BRANCH_AND_BOUND\"]}",
            "application/json");
    });

    svr.Post("/api/optimize", [](const httplib::Request& req, httplib::Response& res) {
        std::vector<Kit> kits; 
        std::vector<int> qtys; 
        std::vector<Truck> trucks;
        parseBody(req.body, kits, qtys, trucks);
        auto physical = expandKits(kits, qtys);
        std::string alg = extractStr(req.body, "algorithm");
        if (alg.empty()) alg = "DYNAMIC_PROGRAMMING";
        auto result = allocateFleet(physical, trucks, alg);
        res.set_content(resultJson(result), "application/json");
    });

    svr.Post("/api/compare", [](const httplib::Request& req, httplib::Response& res) {
        std::vector<Kit> kits; 
        std::vector<int> qtys; 
        std::vector<Truck> trucks;
        parseBody(req.body, kits, qtys, trucks);
        auto physical = expandKits(kits, qtys);

        auto g  = allocateFleet(physical, trucks, "GREEDY");
        auto dp = allocateFleet(physical, trucks, "DYNAMIC_PROGRAMMING");
        auto bb = allocateFleet(physical, trucks, "BRANCH_AND_BOUND");

        std::string body = "{"
            "\"GREEDY\":"              + resultJson(g)  + ","
            "\"DYNAMIC_PROGRAMMING\":" + resultJson(dp) + ","
            "\"BRANCH_AND_BOUND\":"    + resultJson(bb) + "}";
        res.set_content(body, "application/json");
    });

    svr.set_post_routing_handler([](const httplib::Request&, httplib::Response& res) {
        res.set_header("Access-Control-Allow-Origin", "*");
    });
    svr.Options(".*", [](const httplib::Request&, httplib::Response& res) {
        res.set_header("Access-Control-Allow-Origin", "*");
        res.set_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.set_header("Access-Control-Allow-Headers", "Content-Type");
        res.status = 204;
    });

    std::cout << "Server running on http://localhost:" << port
              << "  (frontend: " << frontendDir << ")\n"
              << "  GET  /api/status\n"
              << "  POST /api/optimize\n"
              << "  POST /api/compare\n";

    svr.listen("0.0.0.0", port);
    return 0;
}
