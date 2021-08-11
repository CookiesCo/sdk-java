#!/bin/bash

echo "+++ :java: Building and testing Java SDK...";
set +x;
if ! ./gradlew --no-daemon build check; then
  echo "^^^ +++"
  echo "Build or testsuite failed."
fi
