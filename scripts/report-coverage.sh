#!/bin/bash

echo "--- :gcloud: Downloading codecov token...";
CODECOV_TOKEN=$(gcloud secrets versions access 1 --secret buildbot_javasdk_codecovtoken)

echo "--- :codecov: Reporting coverage...";
set +x;
if ! CODECOV_TOKEN=$CODECOV_TOKEN bash <(curl -s https://codecov.io/bash); then
  echo "^^^ +++"
  echo "Coverage reporting failed."
  exit 1;
fi
