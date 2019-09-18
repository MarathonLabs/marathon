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

DTASK=":publishDefaultPublicationToGitHubRepository"

TARGETS=":core$DTASK :vendor:vendor-android$DTASK :marathon-gradle-plugin$DTASK :report:execution-timeline$DTASK :report:html-report$DTASK :analytics:usage$DTASK"

if [ -n "$TRAVIS_TAG" ]; then
  echo "on a tag -> deploy release version $TRAVIS_TAG"
  ./gradlew $TARGETS -PreleaseMode=RELEASE
else
  echo "not on a tag -> skipping deployment to GitHub"
fi
