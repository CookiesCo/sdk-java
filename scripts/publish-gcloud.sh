#!/bin/bash

source scripts/deploy-common.sh;

echo "--- :gcloud: Publishing to Artifact Registry...";
publish gcloud;
echo "Publish to Artifact Registry complete.";
