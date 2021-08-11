#!/bin/bash

echo "--- :java: Updating docs...";
./gradlew --no-daemon javadoc \
  && rm -fr docs/javadoc \
  && mkdir -p docs/javadoc \
  && cp -fr build/docs/javadoc/* docs/javadoc/;
echo "Docs ready.";
