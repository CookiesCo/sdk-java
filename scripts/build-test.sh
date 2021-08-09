#!/bin/bash

echo ":gradle: Building and testing Java SDK...";
set +x;
./gradlew build check;
