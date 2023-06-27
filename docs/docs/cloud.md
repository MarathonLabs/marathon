---
title: "Overview"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

[Marathon Cloud](https://marathonlabs.io/) is a cloud testing infrastructure built on top of the Marathon test runner.
It automatically provisions virtual devices to accommodate your tests within 15 minutes.
The test execution is then delegated to Marathon test runner, which handles tasks such as batching, 
sorting, preventive retries, and post-factum retries. 
This ensures an even distribution of tests across the provisioned devices. 

## Install

The installation can be performed using [Homebrew](https://brew.sh/):
```shell
brew install malinskiy/tap/marathon-cloud
```
Alternatively, you can download prebuilt binaries for Windows, Linux, or MacOS from [the Release page](https://github.com/MarathonLabs/marathon-cloud-cli/releases).

## API Key

Token creation and management are available at [the Tokens page](https://cloud.marathonlabs.io/tokens). Generate a token and save it somewhere safe for the next step.

## Prepare bundles for testing

### iOS

Marathon Cloud supports tests written with **XCTest and XCUITest frameworks**.
Both the application and the tests must be built for the **ARM architecture**.

Before initiating the testing process for your iOS application, you’ll need to create two `.app` bundles: one for the application that's being tested, and another for the tests themselves. Typically, `debug` variants are utilized for this purpose.

Let's say our project is called "Sample". The code snippet below shows how to build the .app bundle:

```shell
# file structure
# |
# |--home
#    |--john
#       |--sample <== you are here
#          |--sample  <== it's your application
#          ...
#          |--sample.xcodeproj

xcodebuild build-for-testing \
  -project sample.xcodeproj \
  -scheme sample \
  -destination 'platform=iOS Simulator,name=iPhone 14,OS=16.1' \
  -derivedDataPath ./build
```

Note the relative paths of applications, as they will be required for running the tests. In the context of our example and `debug` build, these files can be located at the following paths:

- Application: `/home/john/sample/build/Build/Products/Debug-iphonesimulator/sample.app`
- Test APK: `/home/john/sample/build/Build/Products/Debug-iphonesimulator/sampleUITests-Runner.app`

One important thing to note is that `*.app` files are actually folders in disguise. To transfer them, it's necessary to convert these bundles into `.ipa` format or standard `zip` archives:

```shell
# file structure
# |
# |--home
#    |--john
#       |--sample <== you are here
#          |--build  <== derivedData folder
#          |--sample <== it's your application
#          ...
#          |--sample.xcodeproj
cd build/Build/Products/Debug-iphonesimulator
# convert to zip archive in this format
zip -r sample.zip sample.app
zip -r sampleUITests-Runner.zip sampleUITests-runner.app 
```

Further, we will use these files:

- Application: `/home/john/sample/build/Build/Products/Debug-iphonesimulator/sample.zip`
- Test APK: `/home/john/sample/build/Build/Products/Debug-iphonesimulator/sampleUITests-Runner.zip`

### Android

Marathon Cloud supports tests written with **UIAutomator, Cucumber, Espresso, and [Kaspresso](https://github.com/KasperskyLab/Kaspresso) frameworks**.

Before initiating the testing process for your application, you’ll require two APK files: one for the application that’s being tested, and another for the tests themselves. Typically, `debug` variants are utilized for this purpose.

If the primary application resides under the `app/` subproject, you can execute the following command to build both the app and test APK:

```shell
# file structure
# |
# |--home
#    |--john
#       |--project <== you are here
#          |--app  <== it's your primary application
#          ...
#          |--build.gragle
#          |--settings.gradle  
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
```

Be sure to note the relative paths of the test APK and the app APK, as they will be required for running the tests. In the context of our example, involving the `app` project and the `debug` build, these files can be located at the following paths:

- App APK: `/home/john/project/app/build/outputs/apk/debug/app-debug.apk`
- Test APK: `/home/john/project/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`


## Samples (optional)

To showcase the advantages of Marathon Cloud compared to other solutions, we've prepared a sample app with 300 tests, of which 15% are flaky. During the initial run, our platform will gather information about the tests. During the second run, it will optimize it to fit within 15 minutes.
<Tabs>
<TabItem value="iOS" label="iOS">

```shell
# Download the prebuilt iOS Application
curl https://cloud.marathonlabs.io/samples/ios/sample.zip -o sample.zip

# Download the prebuilt iOS Test Application
curl https://cloud.marathonlabs.io/samples/ios/sampleUITests-Runner.zip -o sampleUITests-Runner.zip
```

</TabItem>	
<TabItem value="Android" label="Android">

```shell
# Download the prebuilt Android Application
curl https://cloud.marathonlabs.io/samples/android/app.apk -o app.apk

# Download the prebuilt Android Test Application
curl https://cloud.marathonlabs.io/samples/android/appTest.apk -o appTest.apk
```

</TabItem>
</Tabs>

## Execution

Now you can start running your tests. Use the following command to execute the CLI with the necessary parameters:

<Tabs>
<TabItem value="iOS" label="iOS">

```shell
marathon-cloud \
	-api-key api_key \
	-app sample.zip \
	-testapp sampleUITests-Runner.zip \
	-platform iOS
```

</TabItem>	
<TabItem value="Android" label="Android">

```shell
marathon-cloud \
	-api-key api_key \
	-app app.apk \
	-testapk appTest.apk \
	-platform Android
```

</TabItem>
</Tabs>

For additional parameters, refer to the [marathon-cloud-cli README](https://github.com/MarathonLabs/marathon-cloud-cli/#installation).