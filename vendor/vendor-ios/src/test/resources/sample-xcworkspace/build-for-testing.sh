#!/usr/bin/env bash

if [[ $# -eq 0 ]]; then 
  UDID="$(xcrun simctl list devices -j | jq -r '.devices | flatten | .[] | select(.availability | match("(?<!un)available")) | select(.name == "iPhone 7") | .udid' | head -1)"
fi

XCODEBUILD_DESTINATION="${1:-${UDID}}"
if [[ -z ${XCODEBUILD_DESTINATION} ]]; then
  echo 1>&2 -e "$(tput setaf 1)ERROR: Required destination simulator id is missing.$(tput sgr0)"
  exit 1
fi

if ! XCODEBUILD="$(command -v xcodebuild)"; then
  echo 1>&2 -e "$(tput setaf 1)ERROR: $(tput bold)xcodebuild$(tput sgr0) $(tput setaf 1)not found$(tput sgr0)"
  exit 1
fi 

echo -e "Building for destination $(tput bold)$XCODEBUILD_DESTINATION$(tput sgr0)" 1>&2
NSUnbufferedIO=YES $XCODEBUILD build-for-testing -derivedDataPath derived-data -workspace sample-app.xcworkspace -scheme UITesting -sdk iphonesimulator -destination "platform=iOS Simulator,id=$XCODEBUILD_DESTINATION" 2>&1
