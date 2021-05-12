#!/usr/bin/env bash
cd $(dirname $0)/..

if [ -z "$GITHUB_MAVEN_USERNAME" ]; then
  echo "error: please set GITHUB_MAVEN_USERNAME environment variable"
  exit 1
fi

if [ -z "$GITHUB_MAVEN_PASSWORD" ]; then
  echo "error: please set GITHUB_MAVEN_PASSWORD environment variable"
  exit 1
fi

if [ -z "$GPG_PASSPHRASE" ]; then
  echo "error: please set GPG_PASSPHRASE environment variable"
  exit 1
fi

TARGETS=""
for i in ":core" ":vendor:vendor-android:base" ":vendor:vendor-android:ddmlib" ":vendor:vendor-android:adam" ":marathon-gradle-plugin" ":report:execution-timeline" ":report:html-report" ":analytics:usage"; do
  TARGETS="$TARGETS $i:publishDefaultPublicationToGitHubRepository"
done

if [ -n "$GIT_TAG_NAME" ]; then
  echo "on a tag -> deploy release version $GIT_TAG_NAME"
  ./gradlew $TARGETS -PreleaseMode=RELEASE
else
  echo "not on a tag -> skipping deployment to GitHub"
fi
