#!/bin/bash

source scripts/deploy-common.sh;

echo "--- :github: Publishing to GitHub Packages...";
publish github;
echo "Publish to GitHub complete.";
