---
title: "Android"
---

### Supported frameworks
Marathon Cloud supports tests written with 
UIAutomator, Cucumber, Espresso, and [Kaspresso](https://github.com/KasperskyLab/Kaspresso) frameworks. 
It is also supports tests written for Flutter apps with [Patrol](https://patrol.leancode.co/) framework. 

### Application and Test Application

Before initiating the testing process for your application, you’ll require two APK files: 
one for the application that’s being tested, and another for the tests themselves. 
Typically, debug variants are utilized for this purpose.

If the primary application resides under the app/ subproject, 
you can execute the following command to build both the app and test APK:

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
Be sure to note the relative paths of the test APK and the app APK, as they will be required for running the tests. 
In the context of our example, involving the `app` project and the `debug` build, these files can be located at the following paths:

- Application APK: `/home/john/project/app/build/outputs/apk/debug/app-debug.apk`
- Test Application APK: `/home/john/project/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
