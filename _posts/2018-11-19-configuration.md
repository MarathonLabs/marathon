---
layout: page
title: "Configuration"
category: doc
date: 2018-11-19 16:55:00
order: 4
---

### CLI
Configuration for CLI is done using the YAML formatted configuration file which by default is placed in the root of the project and named as *Marathonfile*.

Here is a sample Marathonfile with all currently supported options:

```yaml
name: "sample-app tests"
outputDir: "./marathon"
analyticsConfiguration:
  influx:
    url: "http://influx.svc.cluster.local:8086"
    user: "root"
    password: "root"
    dbName: "marathon"
poolingStrategy:
- type: "omni"
- type: "device-model"
- type: "os-version"
- type: "manufacturer"
- type: "abi"
shardingStrategy:
  type: "count"
  count: 5
sortingStrategy:
  type: "success-rate"
  timeLimit: "2015-03-14T09:26:53.590Z"
batchingStrategy:
  type: "fixed-size"
  size: 5
flakinessStrategy:
  type: "probability"
  minSuccessRate: 0.7
  maxCount: 3
  timeLimit: "2015-03-14T09:26:53.590Z"
retryStrategy:
  type: "fixed-quota"
  totalAllowedRetryQuota: 100
  retryPerTestQuota: 2
filteringConfiguration:
  whitelist:
  - type: "simple-class-name"
    regex: ".*"
  - type: "fully-qualified-class-name"
    regex: ".*"
  - type: "method"
    regex: ".*"
  - type: "composition"
    filters:
    - type: "package"
      regex: ".*"
    - type: "method"
      regex: ".*"
    op: "UNION"
  blacklist:
  - type: "package"
    regex: ".*"
  - type: "annotation"
    regex: ".*"
testClassRegexes:
- "^((?!Abstract).)*Test$"
includeSerialRegexes: []
excludeSerialRegexes: []
ignoreFailures: false
isCodeCoverageEnabled: false
fallbackToScreenshots: false
testOutputTimeoutMillis: 30000
debug: true
vendorConfiguration:
  type: "Android"
  androidSdk: "/local/android"
  applicationApk: "kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"
  testApplicationApk: "kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"
  autoGrantPermission: true
vendorConfiguration:
  type: "iOS"
  xctestrunPath: "a/Build/Products/UITesting_iphonesimulator11.0-x86_64.xctestrun"
  derivedDataDir: "a"
  remoteUsername: "testuser"
  remotePrivateKey: "/home/testuser/.ssh/id_rsa"
  knownHostsPath: "known_hosts"
  remoteRsyncPath: "/usr/local/bin/rsync"
  debugSsh: true
```

Each of these options is covered in detail in the [options][1] section. If you're unsure how to properly format your options in Marathonfile take a look at the samples or take a look at the deserialisation logic in the *cli* module of the project. Each option might have a default deserialiser from yaml or a custom one. Usually the custom deserialiser expects the type option to understand which type of strategy we need to instantiate.

### Gradle

Here is an example of gradle config using kotlin:

```kotlin
marathon {
    name = "sample-app tests"
    baseOutputDir = "./marathon"
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
        whitelist {
            add(SimpleClassnameFilter(".*".toRegex()))
        }
        blacklist {
            add(SimpleClassnameFilter("$^".toRegex()))
        }
    }
    testClassRegexes = listOf("^((?!Abstract).)*Test$")
    includeSerialRegexes = emptyList()
    excludeSerialRegexes = emptyList()
    ignoreFailures = false
    isCodeCoverageEnabled = false
    fallbackToScreenshots = false
    testOutputTimeoutMillis = 30_000
    debug = true
    autoGrantPermission = true
}
```

[1]: {{ site.baseurl }}{% post_url 2018-11-19-options %}
