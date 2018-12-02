#!/usr/bin/env bash

UDID="$(xcrun simctl list devices -j | jq -r '.devices | flatten | .[] | select(.name == "iPhone 7") | .udid' | head -1)"

XCODEBUILD_DESTINATION="${1:-${UDID}}"
if [[ -z ${XCODEBUILD_DESTINATION} ]]; then
  echo 1>&2 -e "$(tput setaf 1)ERROR: Required destination simulator id is missing.$(tput sgr0)"
  exit 1
fi

if ! XCODEBUILD="$(command -v xcodebuild)"; then
  echo 1>&2 -e "$(tput setaf 1)ERROR: $(tput bold)xcodebuild$(tput sgr0) $(tput setaf 1)not found$(tput sgr0)"
  exit 1
fi 

$XCODEBUILD build-for-testing -derivedDataPath derived-data -workspace sample-app.xcworkspace -scheme UITesting -sdk iphonesimulator -destination "platform=iOS Simulator,id=$XCODEBUILD_DESTINATION"
