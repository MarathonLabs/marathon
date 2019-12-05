#!/usr/bin/env bash
cd $(dirname $0)/..

if [ -z "$SONATYPE_USERNAME" ]; then
  echo "error: please set SONATYPE_USERNAME environment variable"
  exit 1
fi

if [ -z "$SONATYPE_PASSWORD" ]; then
  echo "error: please set SONATYPE_PASSWORD environment variable"
  exit 1
fi

if [ -z "$GPG_PASSPHRASE" ]; then
  echo "error: please set GPG_PASSPHRASE environment variable"
  exit 1
fi

TARGETS=""
for i in ":core" ":vendor:vendor-android:base" ":vendor:vendor-android:ddmlib" ":marathon-gradle-plugin" ":report:execution-timeline" ":report:html-report" ":analytics:usage"; do
  TARGETS="$TARGETS $i:publishDefaultPublicationToOSSHRRepository"
done

if [ -z "$TRAVIS_TAG" ]; then
  echo "not on a tag -> deploy snapshot version"
  ./gradlew $TARGETS -PreleaseMode=SNAPSHOT
else
  echo "on a tag -> deploy release version $TRAVIS_TAG"
  ./gradlew $TARGETS -PreleaseMode=RELEASE
fi
