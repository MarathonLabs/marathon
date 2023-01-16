---
title: "Configuration"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

## Required CLI configuration

When using CLI you have to specify the *type* of vendor configuration in the root of the **Marathonfile** configuration as following:

```yaml
...
vendorConfiguration:
  type: "Android"
  additional_option1: ...
  additional_option2: ...
```

## Required options

### Android SDK path

:::info

This option is automatically detected if:

1. You're using the Gradle Plugin
2. You're using the CLI and you have an **ANDROID_SDK_ROOT** or **ANDROID_HOME** environment variable

:::

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  androidSdk: "/usr/share/local/android"
```

</TabItem>
</Tabs>

### APK paths

#### Single module testing

##### Application APK path

:::info

This option is automatically detected if you're using Gradle Plugin

:::

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  applicationApk: "dist/app-debug.apk"
```

</TabItem>
</Tabs>

#### Test application APK path

:::info

This option is automatically detected if you're using Gradle Plugin

:::

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  testApplicationApk: "dist/app-debug.apk"
```

</TabItem>
</Tabs>

#### Multi module testing

:::danger

This mode is not supported by Gradle Plugin

:::

Marathon supports testing multiple modules at the same time (e.g. your tests are across more than one module):

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  outputs:
    - application: "dist/app-debug.apk"
      testApplication: "dist/app-debug-androidTest.apk"
    - testApplication: "dist/library-debug-androidTest.apk"  
```

</TabItem>
</Tabs>

Each entry consists of `testApplication` in case of library testing and `application` + `testApplication` for application testing.

#### Split apk testing (Dynamic Feature)

:::danger

This mode is not supported by Gradle Plugin

This mode is also not available for Android devices with version less Android 5.

:::
Marathon supports testing dynamic feature modules:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  applicationApk: "dist/base.apk"
  splitApks:
    - "dist/dynamic-feature1-debug.apk"
    - "dist/dynamic-feature2-debug.apk"
```

</TabItem>
</Tabs>

## Optional

### Automatic granting of permissions

This option will grant all runtime permissions during the installation of the
application. This works like the option ```-g``` for [```adb install```][2] command. By default, it's set to **false**.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  autoGrantPermission: true
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  autoGrantPermission = true
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  autoGrantPermission = true
}
```

</TabItem>
</Tabs>

### ADB initialisation timeout

This option allows you to increase/decrease the default adb init timeout of 30
seconds.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  adbInitTimeoutMillis: 60000
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  adbInitTimeout = 100000
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  adbInitTimeout = 100000
}
```

</TabItem>
</Tabs>

### Device serial number assignment

This option allows to customise how marathon assigns a serial number to devices.
Possible values are:

* ```automatic```
* ```marathon_property```
* ```boot_property```
* ```hostname```
* ```ddms```

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  serialStrategy: "automatic"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  serialStrategy = SerialStrategy.AUTOMATIC
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  serialStrategy = SerialStrategy.AUTOMATIC
}
```

</TabItem>
</Tabs>

Notes on the source of serial number:

```marathon_property``` - Property name `marathon.serialno`

```boot_property``` - Property name `ro.boot.serialno`

```hostname``` - Property name `net.hostname`

```ddms``` - Adb serial number(same as you see with `adb devices` command)

```automatic``` - Sequantially checks all available options for first non empty value.

Priority order:

Before 0.6: ```marathon_property``` -> ```boot_property``` -> ```hostname``` -> ```ddms``` -> UUID

After 0.6:  ```marathon_property``` -> ```ddms``` -> ```boot_property``` -> ```hostname``` -> UUID

### Install options

By default, these will be ```-g -r``` (```-r``` prior to marshmallow). You can specify additional options to append to the default ones.


<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  installOptions: "-d"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  installOptions = "-d"
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  installOptions = "-d"
}
```

</TabItem>
</Tabs>

### Screen recorder configuration

By default, device will record a 1280x720 1Mbps video of up to 180 seconds if it is supported. If on the other hand you want to force
screenshots or configure the recording parameters you can specify this as follows:

:::tip

Android's `screenrecorder` doesn't support videos longer than 180 seconds

:::

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  screenRecordConfiguration:
    preferableRecorderType: "screenshot"
    videoConfiguration:
      enabled: false
      width: 1080
      height: 1920
      bitrateMbps: 2
      timeLimit: 300
    screenshotConfiguration:
      enabled: false
      width: 1080
      height: 1920
      delayMs: 200
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  screenRecordConfiguration = ScreenRecordConfiguration(
    RecorderType.SCREENSHOT,
    VideoConfiguration(
      false, //enabled
      1080, //width
      1920, //height
      2, //Bitrate in Mbps
      300 //Max duration in seconds
    ),
    ScreenshotConfiguration(
      false, //enabled
      1080, //width
      1920, //height
      200 //Delay between taking screenshots
    )
  )
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  screenRecordConfiguration = ScreenRecordConfiguration(
    RecorderType.SCREENSHOT,
    VideoConfiguration(
      false, //enabled
      1080, //width
      1920, //height
      2, //Bitrate in Mbps
      300 //Max duration in seconds
    ),
    ScreenshotConfiguration(
      false, //enabled
      1080, //width
      1920, //height
      200 //Delay between taking screenshots
    )
  )
}
```

</TabItem>
</Tabs>

### Clear state between test batch executions

By default, marathon does not clear state between test batch executions. To mitigate potential test side effects, one could add an option to
clear the package data between test runs. Keep in mind that test side effects might be present.
If you want to isolate tests even further, then you should consider reducing the batch size.

:::caution

Since `pm clear` resets the permissions of the package, the granting of permissions during installation is essentially overridden. Marathon
doesn't grant the permissions again.
If you need permissions to be granted, and you need to clear the state, consider alternatives
like [GrantPermissionRule](https://developer.android.com/reference/androidx/test/rule/GrantPermissionRule)

:::

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  applicationPmClear: true
  testApplicationPmClear: true
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  applicationPmClear = true
  testApplicationPmClear = true
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  applicationPmClear = true
  testApplicationPmClear = true
}
```

</TabItem>
</Tabs>

### Instrumentation arguments

If you want to pass additional arguments to the `am instrument` command executed on the device:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  instrumentationArgs:
    size: small
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  instrumentationArgs {
    set("size", "small")
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  instrumentationArgs {
    set("size", "small")
  }
}
```

</TabItem>
</Tabs>

### [Allure-kotlin][3] support

This option enables collection of allure's data from devices.
Configuration below works out of the box for allure-kotlin 2.3.0+.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  allureConfiguration:
    enabled: true
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  allureConfiguration {
    enabled = true
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  allureConfiguration {
    enabled = true
  }
}
```

</TabItem>
</Tabs>

Additional configuration parameters include **pathRoot** which has two options:

* `EXTERNAL_STORAGE` that is usually the `/sdcard/` on most of the devices
* `APP_DATA` which is usually `/data/data/$appPackage/`

Besides the expected path root, you might need to provide the **relativeResultsDirectory**: this is the relative path to `pathRoot`. The
default path for allure-kotlin as of 2.3.0 is `/data/data/$appPackage/allure-results`.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  allureConfiguration:
    enabled: true
    relativeResultsDirectory: "relative/path/to/allure-results"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  allureConfiguration {
    enabled = true
    relativeResultsDirectory = "relative/path/to/allure-results"
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  allureConfiguration {
    enabled = true
    relativeResultsDirectory = "relative/path/to/allure-results"
  }
}
```

</TabItem>
</Tabs>

Please refer to [allure's documentation][3] on the usage of allure.

:::tip

Starting with allure 2.3.0 your test application no longer needs MANAGE_EXTERNAL_STORAGE permission to write allure's output, so there is no
need to add any special permissions.

:::

Enabling this option effectively creates two allure reports for each test run:

* one from the point of view of the marathon test runner
* one from the point of view of on-device test execution

The on-device report gives you more flexibility and allows you to:

* Take screenshots whenever you want
* Divide large tests into steps and visualise them in the report
* Capture window hierarchy
  and more.

All allure output from devices will be collected under `$output/device-files/allure-results` folder.

### Vendor module selection

The first implementation of marathon for Android relied heavily on AOSP's [ddmlib][4]. For a number of technical reasons we had to write our
own implementation of the ADB client named [adam][6].

:::caution

Ddmlib's implementation is deprecated since marathon **0.7.0** and by default adam is handling all communication
with devices.

By **0.8.0**, ddmlib is going to be removed completely unless we find major issues.

:::

All the features supported in ddmlib's implementation transparently work without any changes. We ask you to test adam prior to the
removal of ddmlib and submit your concerns/issues.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  vendor: ADAM
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  vendor = com.malinskiy.marathon.config.vendor.VendorConfiguration.AndroidConfiguration.VendorType.ADAM
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  vendor = com.malinskiy.marathon.config.vendor.VendorConfiguration.AndroidConfiguration.VendorType.ADAM
}
```

</TabItem>
</Tabs>

### Timeout configuration

With the introduction of [adam][6] we can precisely control the timeout of individual requests. Here is how you can use it:

:::note

This timeout configuration works only in conjuction with adam, ddmlib will not pick up these settings.

:::


<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  vendor: ADAM
  timeoutConfiguration:
    # ISO_8601 duration
    shell: "PT30S"
    listFiles: "PT1M"
    pushFile: "PT1H"
    pushFolder: "PT1H"
    pullFile: "P1D"
    uninstall: "PT1S"
    install: "P1DT12H30M5S"
    screenrecorder: "PT1H"
    screencapturer: "PT1S"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  timeoutConfiguration {
    shell = Duration.ofSeconds(30)
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  timeoutConfiguration {
    shell = Duration.ofSeconds(30)
  }
}
```

</TabItem>
</Tabs>

### Sync/pull files from device after test run

Sometimes you need to pull some folders from each device after the test execution. It may be screenshots or logs or other debug information.
To help with this marathon supports pulling files from devices at the end of the test batch execution. Here is how you can configure it:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  fileSyncConfiguration:
    pull:
      - relativePath: "my-device-folder1"
        aggregationMode: TEST_RUN
        pathRoot: EXTERNAL_STORAGE
      - relativePath: "my-device-folder2"
        aggregationMode: DEVICE
        pathRoot: EXTERNAL_STORAGE
      - relativePath: "my-device-folder3"
        aggregationMode: DEVICE_AND_POOL
        pathRoot: EXTERNAL_STORAGE
      - relativePath: "my-device-folder4"
        aggregationMode: POOL
        pathRoot: EXTERNAL_STORAGE
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  fileSyncConfiguration {
    pull.add(
      FileSyncEntry(
        "my-device-folder1",
        AggregationMode.TEST_RUN,
        PathRoot.EXTERNAL_STORAGE
      )
    )
    pull.add(
      FileSyncEntry(
        "my-device-folder2",
        AggregationMode.DEVICE,
        PathRoot.EXTERNAL_STORAGE
      )
    )
    pull.add(
      FileSyncEntry(
        "my-device-folder3",
        AggregationMode.DEVICE_AND_POOL,
        PathRoot.EXTERNAL_STORAGE
      )
    )
    pull.add(
      FileSyncEntry(
        "my-device-folder4",
        AggregationMode.POOL,
        PathRoot.EXTERNAL_STORAGE
      )
    )
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  fileSyncConfiguration {
    pull.add(
      new FileSyncEntry(
        "my-device-folder1",
        AggregationMode.TEST_RUN,
        PathRoot.EXTERNAL_STORAGE
      )
    )
    pull.add(
      FileSyncEntry(
        "my-device-folder2",
        AggregationMode.DEVICE,
        PathRoot.EXTERNAL_STORAGE
      )
    )
    pull.add(
      FileSyncEntry(
        "my-device-folder3",
        AggregationMode.DEVICE_AND_POOL,
        PathRoot.EXTERNAL_STORAGE
      )
    )
    pull.add(
      FileSyncEntry(
        "my-device-folder4",
        AggregationMode.POOL,
        PathRoot.EXTERNAL_STORAGE
      )
    )
  }
}
```

</TabItem>
</Tabs>

:::caution

Please pay attention to the path on the device: if path root is `EXTERNAL_STORAGE` (which is the default value if you don't specify
anything),
then `relativePath` is relative to the `Environment.getExternalStorageDirectory()` or
the `EXTERNAL_STORAGE` envvar. In practice this means that if you have a folder like `/sdcard/my-folder` you should specify `/my-folder` as
a relative path.

:::

Starting with Android 11 your test application will require **MANAGE_EXTERNAL_STORAGE** permission:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>
  ...
</manifest>
```

Marathon will automatically grant this permission before executing tests if you pull/push files from devices with `EXTERNAL_STORAGE` path
root.

If you don't want to add any permissions to your test application, you can also use the path root `APP_DATA`. This will automatically
transfer the files from your application's private folder, e.g. `/data/data/com.example/my-folder`.

### Push files to device before each batch execution

Sometimes you need to push some files/folders to each device before the test execution. Here is how you can configure it:

:::tip

By default, pushing will be done to `LOCAL_TMP` path root which refers to the `/data/local/tmp`.
You can also push files to `EXTERNAL_STORAGE`. Currently, pushing to `APP_DATA` is not supported.

:::

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  fileSyncConfiguration:
    push:
      - path: "/home/user/folder"
      - path: "/home/user/testfile.txt"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  fileSyncConfiguration {
    push.add(FilePushEntry("/home/user/folder"))
    push.add(FilePushEntry("/home/user/testfile.txt"))
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  fileSyncConfiguration {
    push.add(new FilePushEntry("/home/user/folder"))
    push.add(new FilePushEntry("/home/user/testfile.txt"))
  }
}
```

</TabItem>
</Tabs>

### Test parser

:::tip

If you need to parallelize the execution of parameterized tests or have complex runtime test generation
(custom test runners, e.g. cucumber) - remote parser is your choice.

:::

Test parsing (collecting a list of tests expected to execute) can be done using either a local test parser, which uses byte code analysis,
or a remote test parser that uses an Android device to collect a list of tests expected to run. Both have pros and cons listed below:

| YAML type |    Gradle class    |                                                                                                              Pros |                                                                                                                                                                              Const |
|-----------|:------------------:|------------------------------------------------------------------------------------------------------------------:|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| "local"   | `LocalTestParser`  |                                                                           Doesn't require a booted Android device |                                                         Doesn't support runtime-generated tests, e.g. named parameterized tests. Doesn't support parallelising parameterized tests |
| "remote"  | `RemoteTestParser` | Supports any runtime-generated tests, including parameterized, and allows marathon to parallelise their execution | Requires a booted Android device for parsing. If you need to use annotations for filtering purposes, requires test apk changes as well as attaching a test run listener for parser |

Default test parser is local.

For annotations parsing using remote test parser test run is triggered without running tests (using `-e log true` option). Annotations are
expected to be reported as test metrics, e.g.:

```text
INSTRUMENTATION_STATUS_CODE: 0
INSTRUMENTATION_STATUS: class=com.example.FailedAssumptionTest
INSTRUMENTATION_STATUS: current=4
INSTRUMENTATION_STATUS: id=AndroidJUnitRunner
INSTRUMENTATION_STATUS: numtests=39
INSTRUMENTATION_STATUS: stream=
com.example.FailedAssumptionTest:
INSTRUMENTATION_STATUS: test=ignoreTest
INSTRUMENTATION_STATUS_CODE: 1
INSTRUMENTATION_STATUS: com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer.v2=[androidx.test.filters.SmallTest(), io.qameta.allure.kotlin.Severity(value=critical), io.qameta.allure.kotlin.Story(value=Slow), org.junit.Test(expected=class org.junit.Test$None:timeout=0), io.qameta.allure.kotlin.Owner(value=user2), io.qameta.allure.kotlin.Feature(value=Text on main screen), io.qameta.allure.kotlin.Epic(value=General), org.junit.runner.RunWith(value=class io.qameta.allure.android.runners.AllureAndroidJUnit4), kotlin.Metadata(bytecodeVersion=[I@bdf6b25:data1=[Ljava.lang.String;@46414fa:data2=[Ljava.lang.String;@5d4aab:extraInt=0:extraString=:kind=1:metadataVersion=[I@fbb1508:packageName=), io.qameta.allure.kotlin.Severity(value=critical), io.qameta.allure.kotlin.Story(value=Slow)]
INSTRUMENTATION_STATUS_CODE: 2
INSTRUMENTATION_STATUS: class=com.example.FailedAssumptionTest
INSTRUMENTATION_STATUS: current=4
INSTRUMENTATION_STATUS: id=AndroidJUnitRunner
INSTRUMENTATION_STATUS: numtests=39
INSTRUMENTATION_STATUS: stream=.
INSTRUMENTATION_STATUS: test=ignoreTest
```

To generate the above metrics you need to add a JUnit 4 listener to your dependencies:

```groovy
dependecies {
  androidTestImplementation("com.malinskiy.adam:android-junit4-test-annotation-producer:${LATEST_VERSION}")
}
```

Then you need to attach it to the execution. One way to attach the listener is using `am instrument` parameters, e.g.
`-e listener com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer`. Below you will find an example for configuring a remote
test
parser:

:::caution

Keep in mind that `instrumentationArgs` should include a listener only for the test parser. During the actual execution there is no need
to produce test annotations.

:::

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  testParserConfiguration:
    type: "remote"
    instrumentationArgs:
      listener: "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  testParserConfiguration = TestParserConfiguration.RemoteTestParserConfiguration(
    mapOf(
      "listener" to "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer"
    )
  )
}
```

</TabItem>
</Tabs>

### Test access configuration
:::info

This is power-user feature of marathon that allows setting up GPS location on the emulator, simulating calls, SMS and more thanks to the
access to device-under-test from the test itself.

:::

Marathon supports adam's junit extensions which allow tests to gain access to adb on all devices and emulator's control + gRPC port. See the
[docs](https://malinskiy.github.io/adam/extensions/1-android-junit/) as well as the [PR](https://github.com/Malinskiy/adam/pull/30) for
description on how this works.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  testAccessConfiguration:
    adb: true
    grpc: true
    console: true
    consoleToken: "cantFoolMe"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  testAccessConfiguration = TestAccessConfiguration(
    adb = true,
    grpc = true,
    console = true,
    consoleToken = "cantFoolMe"
  )
}
```

</TabItem>
</Tabs>

### Multiple adb servers

Default configuration of marathon assumes that adb server is started locally and is available at `127.0.0.1:5037`. In some cases it may be
desirable to connect multiple adb servers instead of connecting devices to a single adb server. An example of this is distributed execution
of tests using test access (calling adb commands from tests). For such scenario all emulators should be connected via a local (in relation
to the emulator) adb server. Default port for each host is 5037.

:::info

This functionality is only supported by vendor adam because ddmlib doesn't support connecting to a remote instance of adb server.

:::

:::tip

Adb server started on another machine should be exposed to external traffic, e.g. using option `-a`. For example, if you want to
expose the adb server and start it in foreground explicitly on port 5037: `adb nodaemon server -a -P 5037`.

:::


<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  adbServers:
    - host: 127.0.0.1
    - host: 10.0.0.2
      port: 5037
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  adbServers = listOf(
    AdbEndpoint(host = "127.0.0.1"),
    AdbEndpoint(host = "10.0.0.2", port = 5037)
  )
}
```

</TabItem>
</Tabs>

### Extra applications APK path

Install extra apk before running the tests if required, e.g. test-butler.apk

:::caution

For Gradle Plugin users, the `extraApplications` parameter will affect all the testing apk configurations in a single module.

:::

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  extraApplicationsApk:
    - "dist/extra.apk"
    - "dist/extra_two.apk"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  extraApplications = listOf(
    File(project.rootDir, "test-butler-app-2.2.1.apk")
      File ("/home/user/other-apk-with-absolute-path.apk"),
  )
}
```

</TabItem>
</Tabs>

### AndroidX ScreenCapture API

Marathon supports automatic pull of screenshots taken
via [ScreenCapture API](https://developer.android.com/reference/androidx/test/runner/screenshot/package-summary)

To enable marathon to pull screenshots you need to use a
custom [ScreenCaptureProcessor](https://developer.android.com/reference/androidx/test/runner/screenshot/ScreenCaptureProcessor) called
`AdamScreenCaptureProcessor`.

Firstly, add `com.malinskiy.adam:android-junit4-test-annotation-producer:${LATEST_VERSION}` to your test code.

Secondly, enable AdamScreenCaptureProcessor in your tests. You can do this manually:

```kotlin
Screenshot.addScreenCaptureProcessors(setOf(AdamScreenCaptureProcessor()))
```

or using JUnit4 rule `AdamScreenCaptureRule`:

```kotlin
class ScreenshotTest {
  @get:Rule
  val screencaptureRule = AdamScreenCaptureRule()

  @Test
  fun testScreencapture() {
    Screenshot.capture().process()
  }
}

```

That's it, you're done. No need for custom configuration on Marathon's side: everything should be picked up automatically.

More information on this custom ScreenCaptureProcessor can be
found [here](https://malinskiy.github.io/adam/extensions/2-android-event-producers/#adamscreencaptureprocessor).

### Enable window animations

By default, marathon uses `--no-window-animation` flag. Use the following option if you want to enable window animations:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
vendorConfiguration:
  type: "Android"
  disableWindowAnimation: false
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  disableWindowAnimation = false
}
```

</TabItem>
</Tabs>

[1]: https://developer.android.com/studio/

[2]: https://developer.android.com/studio/command-line/adb#issuingcommands

[3]: https://github.com/allure-framework/allure-kotlin

[4]: https://android.googlesource.com/platform/tools/base/+/master/ddmlib

[5]: https://medium.com/@Malinskiy/adam-a-second-birth-to-androids-ddmlib-c90fdde4c39d

[6]: https://github.com/Malinskiy/adam
