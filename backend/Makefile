CXX      = clang++
CXXFLAGS = -std=c++17 -O2 -Wall -Isrc -pthread
BIN      = ../bin/relief_app

.PHONY: all clean run

all: $(BIN)

../bin:
	@mkdir -p ../bin

$(BIN): src/algorithms.cpp src/main.cpp | ../bin
	@echo "Building..."
	@$(CXX) $(CXXFLAGS) src/algorithms.cpp src/main.cpp -o $(BIN)
	@echo "Done — $(BIN)"

run: all
	@$(BIN)

clean:
	@rm -f $(BIN)
	@echo "Cleaned."
