#!/usr/bin/env bash
cd `dirname $0`/..

DTASK=":publishDefaultPublicationToMavenLocal"
TARGETS=":core$DTASK :vendor:vendor-android$DTASK :marathon-gradle-plugin$DTASK :report:execution-timeline$DTASK :report:html-report$DTASK :analytics:usage$DTASK"

if [ ! -z "$TRAVIS_TAG" ]
then
    echo "on a tag -> deploy release version $TRAVIS_TAG"
    ./gradlew $TARGETS -PreleaseMode=RELEASE
else
    echo "not on a tag -> deploy snapshot version"
    ./gradlew $TARGETS -PreleaseMode=SNAPSHOT
fi
