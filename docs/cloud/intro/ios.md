---
title: "iOS"
---

### Supported frameworks
Marathon Cloud supports tests written with **XCTest and XCUITest frameworks**.
Both the application and the tests must be built for the **ARM architecture**.

### Application and Test Application

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
# convert to zip archive in this example
zip -r sample.zip sample.app
zip -r sampleUITests-Runner.zip sampleUITests-runner.app 
```

Further, we will use these files:

- Application: `/home/john/sample/build/Build/Products/Debug-iphonesimulator/sample.zip`
- Test APK: `/home/john/sample/build/Build/Products/Debug-iphonesimulator/sampleUITests-Runner.zip`

