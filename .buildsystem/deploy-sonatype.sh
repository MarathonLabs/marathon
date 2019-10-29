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

DTASK=":publishDefaultPublicationToOSSHRRepository"

TARGETS=":core$DTASK :vendor:vendor-android$DTASK :marathon-gradle-plugin$DTASK :report:execution-timeline$DTASK :report:html-report$DTASK :analytics:usage$DTASK"

if [ -z "$TRAVIS_TAG" ]; then
  echo "not on a tag -> deploy snapshot version"
  ./gradlew $TARGETS -PreleaseMode=SNAPSHOT
else
  echo "on a tag -> deploy release version $TRAVIS_TAG"
  ./gradlew $TARGETS -PreleaseMode=RELEASE
fi
