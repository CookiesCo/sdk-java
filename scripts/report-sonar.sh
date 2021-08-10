#!/bin/bash

echo "--- :gcloud: Downloading sonar token...";
SONAR_TOKEN=$(gcloud secrets versions access 1 --secret buildbot_javasdk_sonartoken)

echo "--- :sonarcloud: Reporting analysis...";
set +x;
if ! SONAR_TOKEN=$SONAR_TOKEN ./gradlew --no-daemon sonarqube; then
  echo "^^^ +++"
  echo "Sonar reporting failed."
  exit 1;
fi
