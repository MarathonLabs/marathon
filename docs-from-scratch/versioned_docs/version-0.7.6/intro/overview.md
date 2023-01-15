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

Create a basic **Marathonfile** in the root of your project with the following content (example for Android)
```yaml
name: "My application"
outputDir: "build/reports/marathon"
vendorConfiguration:
  type: "Android"
  applicationApk: "dist/debug/app-debug.apk"
  testApplicationApk: "dist/debug/app-debug-androidTest.apk"
```

Vendor section describes platform specific details, for iOS a simple configuration would look like:

```yaml
name: "My application"
outputDir: "derived-data/Marathon"
testClassRegexes: ["^((?!Abstract).)*Tests$"]
vendorConfiguration:
  type: "iOS"
  derivedDataDir: "derived-data"
  sourceRoot: "sample-appUITests"
  knownHostsPath: ${HOME}/.ssh/known_hosts
  remoteUsername: ${USER}
  remotePrivateKey: ${HOME}/.ssh/marathon
```

Since iOS doesn't have any way to discover remote execution devices you have to provide your remote simulators using the **Marathondevices** file:

```yaml
- host: "10.0.0.2"
  udid: "72F9E06F-FBFD-4639-8157-577A0B2E9FDF"
```

This **Marathondevices** file specifies a list of macOS instances and the UDIDs of simulators for use. All the hosts are accessed via ssh and
require a non-interactive authentication. Even if you login to localhost you still require a private key that will pass the authentication.
Please make sure before executing the test run that you can ssh into all the `host`s listed in the **Marathondevices**:
```bash
$ ssh 10.0.0.2
```   

## Execution

Start the test runner in the root of your project
```bash
$ marathon 
XXX [main] INFO com.malinskiy.marathon.cli.ApplicationView - Starting marathon
XXX [main] INFO com.malinskiy.marathon.cli.config.ConfigFactory - Checking Marathonfile config
...
```

# Getting Started
Start by visiting the [Download and Setup][1] page to learn how to integrate Marathon into your project.

Then take a look at [Configuration][2] page to learn the basics of configuration.

For more help and examples continue through the rest of the Documentation section, or take a look at one of our [sample apps][3].

# Requirements
Marathon requires Java Runtime Environment 8 or higher.

[1]: /intro/install
[2]: /intro/configure
[3]: https://github.com/MarathonLabs/marathon/tree/develop/sample
