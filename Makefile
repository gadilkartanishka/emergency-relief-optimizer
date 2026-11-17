FRONTEND = $(shell pwd)/frontend

.PHONY: all build server clean

all build:
	$(MAKE) -C backend all

server: all
	@bin/relief_app 8080 $(FRONTEND)

clean:
	$(MAKE) -C backend clean
