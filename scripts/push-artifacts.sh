#!/bin/bash

echo "--- :buildkite: Preparing artifacts...";
tar -czvf build/test-reports.tar.gz "./build/reports/tests/test";
tar -czvf build/coverage-reports.tar.gz "./build/reports/jacoco/test";

echo "--- :buildkite: Pushing artifacts to Buildkite...";
buildkite-agent artifact upload "build/reports/jacoco/test/jacocoTestReport.xml";
buildkite-agent artifact upload "build/libs/*.jar";
buildkite-agent artifact upload "build/test-reports.tar.gz";
buildkite-agent artifact upload "build/reports/tests/dash-test.tar.gz";
buildkite-agent artifact upload "build/coverage-reports.tar.gz";

if [ -z ${BUILDKITE_TAG+x} ];
then ARTIFACT_BASE="$BUILDKITE_TAG";
else ARTIFACT_BASE="$BUILDKITE_BRANCH"; fi

echo "+++ :gcloud: Pushing artifacts to GCS (base: '$ARTIFACT_BASE')...";

cd build/libs && gsutil cp "./*.jar" "gs://cookies-sdk/ci/java/$ARTIFACT_BASE/latest/" && cd ../..;
cd build/reports/tests/test && gsutil cp "./*" "gs://cookies-sdk/ci/java/$ARTIFACT_BASE/latest/reports/tests/" && cd ../../../..;
cd build/reports/jacoco/test && gsutil cp "./*" "gs://cookies-sdk/ci/java/$ARTIFACT_BASE/latest/reports/coverage/" && cd ../../../..;
echo "Publish to GCS complete.";
