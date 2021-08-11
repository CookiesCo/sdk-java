
echo "--- :gcloud: Downloading release keys...";
SIGNING_KEY_ID="D761AD21";
SIGNING_KEY="$(gcloud secrets versions access 1 --secret buildbot_gpg_key)";
SIGNING_KEY_PASSWORD="$(gcloud secrets versions access 1 --secret buildbot_gpg_keypassword)";

function publish {
    ORG_GRADLE_PROJECT_signingKey="$SIGNING_KEY" \
    ORG_GRADLE_PROJECT_signingKeyId="$SIGNING_KEY_ID" \
    ORG_GRADLE_PROJECT_signingPassword="$SIGNING_KEY_PASSWORD" \
      ./gradlew --no-daemon publish -PartifactRepository=$1 $2;
}

function sonatypePublish {
    ORG_GRADLE_PROJECT_signingKey="$SIGNING_KEY" \
    ORG_GRADLE_PROJECT_signingKeyId="$SIGNING_KEY_ID" \
    ORG_GRADLE_PROJECT_signingPassword="$SIGNING_KEY_PASSWORD" \
      ./gradlew --no-daemon publishToSonatype closeSonatypeStagingRepository $1;
}
