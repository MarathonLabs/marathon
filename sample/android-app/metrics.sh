#!/usr/bin/env bash

./gradlew assemble assembleAndroidTest
adb uninstall com.example.test
adb uninstall com.example
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk          
adb install -r app/build/outputs/apk/debug/app-debug.apk

adb shell am instrument -w -r -e class com.example.MainActivityTest  com.example.test/androidx.test.runner.AndroidJUnitRunner
