# Introduction

Marathon will help you execute tests in the shortest time possible. Here is a 2 minute guide on how to get started:

## Install
```shell
brew tap malinskiy/tap
brew install malinskiy/tap/marathon
```

## Configure
Configuration is done via a yaml file. By default marathon will look for a file named `Marathonfile`. Here is an example for Android to start with. Place it in the root of your project:
```yaml
name: "My awesome tests"
outputDir: "marathon"
debug: false
vendorConfiguration:
  type: "Android"
  applicationApk: "app/build/outputs/apk/debug/app-debug.apk"
  testApplicationApk: "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
```
:::tip

Don't forget to replace the **applicationApk** and **testApplicationApk**

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

For CI there is a JUnit xml `marathon_junit_report.xml` generated in the `$output/tests/omni` folder:
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
