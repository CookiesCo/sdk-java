#!/bin/bash

echo "--- :buildkite: Loading artifacts...";
buildkite-agent artifact download "build/reports/*" --step build-test-linux .;
buildkite-agent artifact download "build/libs/*" --step build-test-linux .;
