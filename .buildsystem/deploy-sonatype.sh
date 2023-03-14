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

ASSEMBLE_TARGETS=""
PUBLISH_TARGETS=""
for i in ":core" ":vendor:vendor-android" ":marathon-gradle-plugin" ":report:execution-timeline" ":report:html-report" ":analytics:usage" ":configuration"; do
  ASSEMBLE_TARGETS="$ASSEMBLE_TARGETS $i:assemble"
  PUBLISH_TARGETS="$PUBLISH_TARGETS $i:publishAllPublicationsToOSSHRRepository"
done

if [ -z "$GIT_TAG_NAME" ]; then
  echo "not on a tag -> deploy snapshot version"
  ./gradlew $ASSEMBLE_TARGETS -PreleaseMode=SNAPSHOT
  ./gradlew $PUBLISH_TARGETS -PreleaseMode=SNAPSHOT
else
  echo "on a tag -> deploy release version $GIT_TAG_NAME"
  ./gradlew $ASSEMBLE_TARGETS -PreleaseMode=RELEASE
  ./gradlew $PUBLISH_TARGETS -PreleaseMode=RELEASE
fi
