import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# 简介

Marathon将帮助你在最短的时间内执行测试。下面是一个关于如何开始的2分钟指南：

## 安装
```shell
brew tap malinskiy/tap
brew install malinskiy/tap/marathon
```

:::tip

如果你没有安装自酿啤酒，请到[https://brew.sh](https://brew.sh/)了解如何安装的说明  

:::

## 配置
配置是通过一个yaml文件完成的。默认情况下，marathon会寻找一个名为`Marathonfile`的文件。 

例如，将以下内容放在你的项目根目录下的`Marathonfile`中：
<Tabs>
<TabItem value="Android" label="Android">

```yaml
name: "我很棒的测试"
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
testClassRegexes: ["^((?!Abstract).)*Tests$"]
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

不要忘记替换**applicationApk**、**testApplicationApk**或**application**、**testApplication**。

iOS还需要一个关于你的设备的小型配置文件，[更多信息请看这里][1］

:::

## 执行
连接执行设备，例如，对于Android来说，这将是一个物理手机或一个模拟器，然后执行测试：

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

## 分析结果
在执行后，马拉松将打印出测试运行的总结，给出执行过程中发生的一般概述：
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

对于CI来说，有一个JUnit xml `marathon_junit_report.xml`在`$outputDir/tests/omni`文件夹中生成，其中`$outputDir`是你在[marathon configuration]（/intro/configure#output-directory）中定义的目录：
```shell-session 
foo@bar:~$ cat marathon/omni/marathon_junit_report.xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="omni" tests="40" failures="15" errors="0" time="71.093" skipped="3"
  timestamp="2023-01-13T05:53:59">
  <testcase name="testUnsafeAccess" time="1.357" classname="com.example.AdbActivityTest">
...
```

还有各种各样的html报告供你分析：
![html报告](/img/screenshot-html-report-1.png)
![allure报告](/img/screenshot-allure-report-1.png)

## 接下来的步骤
你可以为你的测试运行做更多的定制和优化，这可以帮助你加快测试执行和/或解决可靠性问题。继续阅读文档，了解马拉松如何帮助你。

[1]: /ios/workers.md
