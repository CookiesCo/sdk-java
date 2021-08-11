#!/bin/bash

source scripts/deploy-common.sh;

echo "--- :gcloud: Publishing to OSSRH (snapshots)...";
sonatypePublish "-PuseStaging=true -Pungate-stage=true";
echo "Publish to OSSRH snapshots complete. Please test and approve snapshots via Nexus UI.";
