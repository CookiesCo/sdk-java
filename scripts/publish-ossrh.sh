#!/bin/bash

source scripts/deploy-common.sh;

echo "--- :maven: Publishing to Maven Central...";
sonatypePublish "-Pungate=true";
echo "Publish to Maven Central complete. Please test and approve snapshots via Nexus UI.";
