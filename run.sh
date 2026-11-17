#!/usr/bin/env bash
set -e
make -s
./bin/relief_app "$@"
