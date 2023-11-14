---
title: "Gradle Plugin"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

:::caution

Gradle plugin is a just thin wrapper around CLI. It bundles the CLI and installs it on-the-fly.
You **should** try using [CLI][2] first

:::

# Tradeoffs using Gradle Plugin

| Pros                                     | Cons                                                                                      |
|------------------------------------------|-------------------------------------------------------------------------------------------|
| Configuration using Gradle syntax        | Requires project sync before testing starts                                               |
| No installation of marathon CLI required | Less flexibility in choosing AGP+Gradle versions. CLI is independent of your Gradle setup |
|                                          | Easier to manage when you have more than 1 test run configuration                         |
|                                          | Missing features, e.g. multi-module testing                                               |

## Install

Marathon gradle plugin is published to [plugins.gradle.org][1].
To apply the plugin:

<Tabs>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
plugins {
  id("com.malinskiy.marathon") version "X.X.X"
}
```

</TabItem>
<TabItem value="GroovyDSL" label="Groovy DSL">

```groovy
plugins {
  id 'com.malinskiy.marathon' version 'X.X.X'
}
```

</TabItem>
</Tabs>

All the test tasks will start with **marathon** prefix, for example **marathonDebugAndroidTest**.

[1]: https://plugins.gradle.org
[2]: /intro/install.md

## Configure

Configuration for Gradle Plugin can only be done via Gradle DSL, i.e. you can't use Marathonfile as configuration when running tests using Gradle Plugin.

Here is an example of gradle config using Kotlin DSL:

```kotlin
marathon {
  name = "sample-app tests"
  baseOutputDir = "./marathon"
  outputConfiguration {
    maxPath = 1024
  }
  analytics {
    influx {
      url = "http://influx.svc.cluster.local:8086"
      user = "root"
      password = "root"
      dbName = "marathon"
    }
  }
  poolingStrategy {
    operatingSystem = true
  }
  shardingStrategy {
    countSharding {
      count = 5
    }
  }
  sortingStrategy {
    executionTime {
      percentile = 90.0
      executionTime = Instant.now().minus(3, ChronoUnit.DAYS)
    }
  }
  batchingStrategy {
    fixedSize {
      size = 10
    }
  }
  flakinessStrategy {
    probabilityBased {
      minSuccessRate = 0.8
      maxCount = 3
      timeLimit = Instant.now().minus(30, ChronoUnit.DAYS)
    }
  }
  retryStrategy {
    fixedQuota {
      totalAllowedRetryQuota = 200
      retryPerTestQuota = 3
    }
  }
  filteringConfiguration {
    allowlist {
      add(SimpleClassnameFilterConfiguration(".*".toRegex()))
    }
    blocklist {
      add(SimpleClassnameFilterConfiguration("$^".toRegex()))
    }
  }
  testClassRegexes = listOf("^((?!Abstract).)*Test$")
  includeSerialRegexes = emptyList()
  excludeSerialRegexes = emptyList()
  uncompletedTestRetryQuota = 100
  ignoreFailures = false
  isCodeCoverageEnabled = false
  fallbackToScreenshots = false
  testOutputTimeoutMillis = 30_000
  debug = true
  autoGrantPermission = true
}
```

## Execute

Executing your tests via gradle is done via calling generated marathon gradle task, for example *marathonDebugAndroidTest*. 
These tasks will be created for all testing flavors including multi-dimension setup.

```shell-session
foo@bar $ gradle :app:marathonDebugAndroidTest
```
