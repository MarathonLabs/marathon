import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Introduction

Marathon will help you execute tests in the shortest time possible. Here is a 2 minute guide on how to get started:

## Install
```shell
brew tap malinskiy/tap
brew install malinskiy/tap/marathon
```

:::tip

If you don't have homebrew installed head over to [https://brew.sh](https://brew.sh/) for instructions on how to install it 

:::

## Configure
Configuration is done via a yaml file. By default marathon will look for a file named `Marathonfile`. 

For example, place the following contents in the `Marathonfile` in the root of your project:
<Tabs>
<TabItem value="Android" label="Android">

```yaml
name: "My awesome tests"
outputDir: "marathon"
debug: false
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

:::tip

Don't forget to replace the **applicationApk**, **testApplicationApk** or **application**, **testApplication**

iOS also requires a small configuration file about your devices, [see here for more information][1]

:::

## Execute
Connect execution devices, e.g. for Android this would be a physical phone or an emulator, then execute the tests:

```shell-session
foo@bar:~$ marathon
00% | [omni]-[127.0.0.1:5037:emulator-5554] com.example.AdbActivityTest#testUnsafeAccess started
03% | [omni]-[127.0.0.1:5037:emulator-5554] com.example.AdbActivityTest#testUnsafeAccess failed
03% | [omni]-[127.0.0.1:5037:emulator-5554] com.example.ScreenshotTest#testScreencapture started
05% | [omni]-[127.0.0.1:5037:emulator-5554] com.example.ScreenshotTest#testScreencapture failed
05% | [omni]-[127.0.0.1:5037:emulator-5554] com.example.ParameterizedTest#test[0] started
08% | [omni]-[127.0.0.1:5037:emulator-5554] com.example.ParameterizedTest#test[0] ended
...
```

## Analyze results
After the execution marathon will print out a summary of the test run which gives a generic overview of what happened during the execution:
```shell-session
foo@bar:~$ marathon
...
...
...
Marathon run finished:
Device pool omni:
        22 passed, 15 failed, 3 ignored tests
        Failed tests:
                com.example.MainActivityFlakyTest#testTextFlaky
                ...
        Flakiness overhead: 1849ms
        Raw: 22 passed, 15 failed, 3 ignored, 6 incomplete tests
                com.example.MainActivityFlakyTest#testTextFlaky failed 1 time(s)
        Incomplete tests:
                com.example.BeforeTestFailureTest#testThatWillNotSeeTheLightOfDay incomplete 3 time(s)
Total time: 0H 1m 45s
Marathon execution failed
```

For CI there is a JUnit xml `marathon_junit_report.xml` generated in the ``$outputDir/tests/omni`` folder where ``$outputDir`` is a directory that you’ve defined in the [marathon configuration](intro/configure#output-directory):
```shell-session 
foo@bar:~$ cat marathon/omni/marathon_junit_report.xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="omni" tests="40" failures="15" errors="0" time="71.093" skipped="3"
  timestamp="2023-01-13T05:53:59">
  <testcase name="testUnsafeAccess" time="1.357" classname="com.example.AdbActivityTest">
...
```

There is also an assortment of html reports for you to analyze:
![html report](/img/screenshot-html-report-1.png)
![allure report](/img/screenshot-allure-report-1.png)

## Next steps
There are many more customisations and optimisation that you can do for your test runs that can help you speed up the test execution and/or battle reliability issues. Continue reading the docs to understand how marathon can help you. 

[1]: apple/workers.md
