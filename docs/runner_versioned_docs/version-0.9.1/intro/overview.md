import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Overview

Marathon is a fast, platform-independent test runner focused on performance and stability. It offers easy to use platform implementations for Android and iOS as well as an API for use with custom hardware farms.

Marathon implements multiple key concepts of test execution such as test **batching**, **device pools**, test **sharding**, test **sorting**, **preventive retries** as well as **post-factum retries**. By default, most of these are set to conservative defaults but custom configurations are encouraged for those who want to optimize performance and/or stability.

Marathon's primary focus is on **full control over the balance between stability of test execution and the overall test run performance**.

# Performance
Marathon takes into account two key aspects of test execution:
* The duration of the test
* The probability of the test passing

Test run can only finish as quickly as possible if we plan the execution of tests with regard to the expected duration of the test. On the other hand, we need to address the flakiness of the environment and of the test itself. One key indicator of flakiness is the *probability* of the test passing.

Marathon takes a number of steps to ensure that each test run is as balanced as possible:
* The flakiness strategy queues up preventive retries for tests which are expected to fail during the test run according to the current real-time statistical data
* The sorting strategy forces long tests to be executed first so that if an unexpected retry attempt occurs it doesn't affect the test run significantly (e.g. at the end of execution)
* If all else fail we revert back to post-factum retries, but we try to limit their impact on the run with retry quotas

## Configuration

Create a basic **Marathonfile** in the root of your project with the following content:
<Tabs>
<TabItem value="Android" label="Android">

```yaml
name: "My application"
outputDir: "build/reports/marathon"
vendorConfiguration:
  type: "Android"
  applicationApk: "dist/app-debug.apk"
  testApplicationApk: "dist/app-debug-androidTest.apk"
```

</TabItem>
<TabItem value="iOS" label="iOS">

```yaml
name: "My application"
outputDir: "derived-data/Marathon"
vendorConfiguration:
  type: "iOS"
  bundle:
    application: "sample.app"
    testApplication: "sampleUITests.xctest"
    testType: xcuitest
```

</TabItem>
</Tabs>


Vendor section describes platform specific details.

Since iOS doesn't have any way to discover remote execution devices you have to provide your remote simulators using the **Marathondevices** file:

```yaml
workers:
  - transport:
      type: local
    devices:
      - type: simulator
        udid: "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
```

This **Marathondevices** file specifies a list of macOS instances and simulators for use. Marathon can use pre-provisioned simulators, but it can also provision new ones if needed.

Example above uses the local instance where marathon is executed, but you can connect many more instance via SSH. 

:::tip

The instance where you run marathon is not limited to macOS: if you're using remote macOS instances then
you can easily start your marathon run from Linux for example.

:::

You can find more information on providing devices in the [workers documentation][1]

The file structure for testing should look something like this:

<Tabs>
<TabItem value="Android" label="Android">

```shell-session
foo@bar $ tree .  
.
├── Marathondevices
├── Marathonfile
├── dist
│   ├── app-debug.apk
│   ├── app-debug-androidTest.apk
```

</TabItem>
<TabItem value="iOS" label="iOS">

```shell-session
foo@bar $ tree .  
.
├── Marathondevices
├── Marathonfile
├── build
│   ├── my.app
│   ├── my.xctest

```

</TabItem>
</Tabs>

## Execution

Start the test runner in the root of your project
```bash
$ marathon 
XXX [main] INFO com.malinskiy.marathon.cli.ApplicationView - Starting marathon
XXX [main] INFO com.malinskiy.marathon.cli.config.ConfigFactory - Checking Marathonfile config
...
```

# Getting Started
Start by visiting the [Download and Setup][2] page to learn how to integrate Marathon into your project.

Then take a look at [Configuration][3] page to learn the basics of configuration.

For more help and examples continue through the rest of the Documentation section, or take a look at one of our [sample apps][4].

# Requirements
Marathon requires Java Runtime Environment 8 or higher.

[1]: ../ios/workers.md
[2]: ../intro/install.md
[3]: ../intro/configure.md
[4]: https://github.com/MarathonLabs/marathon/tree/develop/sample
