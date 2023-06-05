import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# 概述

Marathon是一个快速、独立于平台的测试运行器，专注于性能和稳定性。它为Android和iOS提供了易于使用的平台实现，同时也提供了用于定制硬件农场的API。

马拉松实现了测试执行的多个关键概念，如测试**分批**、**设备池**、测试**分片**、测试**排序**、**预防性重试以及**事实后重试。在默认情况下，大部分都被设置为保守的默认值，但对于那些想要优化性能和/或稳定性的人来说，鼓励进行自定义配置。

Marathon的主要重点是**完全控制测试执行的稳定性和整体测试运行性能之间的平衡**。

# 性能
Marathon考虑到了测试执行的两个关键方面：
* 测试的持续时间
*测试通过的概率

只有当我们在计划测试执行时考虑到测试的预期持续时间，测试运行才能尽可能快地完成。另一方面，我们需要解决环境和测试本身的飘忽性。脆弱性的一个关键指标是测试通过的*概率。

马拉松采取了一些步骤来确保每个测试运行尽可能的平衡：
* 脆弱度策略根据当前的实时统计数据，为那些在测试运行中预计会失败的测试排队进行预防性重试。
* 排序策略迫使长的测试先被执行，这样，如果发生意外的重试尝试，就不会对测试运行产生重大影响（例如，在执行结束时）。
* 如果一切都失败了，我们会恢复到事后重试，但我们试图用重试配额限制它们对运行的影响。

## 配置

在你的项目根部创建一个基本的**马拉松文件**，内容如下：
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


供应商部分描述了平台的具体细节。

由于iOS没有任何方法来发现远程执行设备，你必须使用**Marathondevices**文件来提供你的远程模拟器：

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

这个**Marathondevices**文件指定了一个供使用的macOS实例和模拟器的列表。马拉松可以使用预先提供的模拟器，但如果需要，它也可以提供新的模拟器。

上面的例子使用了执行马拉松的本地实例，但你可以通过SSH连接更多的实例。

:::tip

你运行马拉松的实例并不局限于macOS：如果你使用的是远程macOS的实例，那么
你可以很容易地从Linux启动你的马拉松运行，例如。

:::

你可以在[工作者文档][1]中找到更多关于提供设备的信息。

用于测试的文件结构应该是这样的：

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

## 执行

在你项目的根目录下启动测试运行器
```bash
$ marathon 
XXX [main] INFO com.malinskiy.marathon.cli.ApplicationView - Starting marathon
XXX [main] INFO com.malinskiy.marathon.cli.config.ConfigFactory - Checking Marathonfile config
...
```

# 入门
首先访问 [Download and Setup][2] 页面，了解如何将马拉松集成到你的项目中。

然后看一下[Configuration][3]页面，学习配置的基本知识。

要想获得更多的帮助和例子，请继续浏览文档部分的其他内容，或者看看我们的一个[样本应用程序][4]。

# 要求
Marathon需要Java Runtime Environment 8或更高版本。

[1]: /ios/workers.md
[2]: /intro/install.md
[3]: /intro/configure.md
[4]: https://github.com/MarathonLabs/marathon/tree/develop/sample
