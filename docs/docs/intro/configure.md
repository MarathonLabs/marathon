---
title: "Configuration"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Configuration of marathon is done using a YAML formatted configuration usually placed in the root of the project and named
**Marathonfile**.

Below is a very simple example of Marathonfile (without the platform configuration):

```yaml
name: "My awesome tests"
outputDir: "marathon"
debug: false
```

There are _a lot_ of options in marathon. This can be overwhelming especially when you're just starting out. We will split the options into 
general options below, complex options that you can find as subsections in the menu on the left and platform-specific options under each 
platform section.

If you're unsure how to properly format your options in Marathonfile take a look at the samples or take a look at the [deserialisation logic][1] in the *configuration* module of the project. 
Each option might use a default deserializer from yaml or a custom one. Usually the custom deserializer expects the _type_ option for polymorphic types to 
understand which specific object we need to instantiate.

## Important notes
### File-system path handling

When specifying **relative host file** paths in the configuration they will be resolved relative to the directory of the Marathonfile, e.g. if
you have `/home/user/app/Marathonfile` with `baseOutputDir = "./output"` then the actual path to the output directory will
be `/home/user/app/output`.

## Required
Below you will find a list of currently supported configuration parameters and examples of how to set them up. Keep in mind that not all of the
additional parameters are supported by each platform. If you find that something doesn't work - please submit an issue for a
platform at fault.

### Test run configuration name

This string specifies the name of this test run configuration. It is used mainly in the generated test reports.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
name: "My test run for sample app"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  name = "My test run for sample app"
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  name = "My test run for sample app"
}
```

</TabItem>
</Tabs>

### Output directory

Directory path to use as the root folder for all the runner output (logs, reports, etc).

For gradle, the output path will automatically be set to a `marathon` folder in your reports folder unless it's overridden.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
outputDir: "build/reports/marathon"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  baseOutputDir = "some-path"
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  baseOutputDir = "some-path"
}
```

</TabItem>
</Tabs>

### Platform-specific options
Marathon requires you to specify the platform for each run, for example:
```yaml
vendorConfiguration:
  type: "Android"
```

Refer to platform configuration for additional options inside the `vendorConfiguration` block.

## Optional

### Test class regular expression
By default, test classes are found using the ```"^((?!Abstract).)*Test[s]*$"``` regex. You can override this if you need to.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
testClassRegexes:
  - "^((?!Abstract).)*Test[s]*$"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  testClassRegexes = listOf(
    "^((?!Abstract).)*Test[s]*$"
  )
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  testClassRegexes = [
    "^((?!Abstract).)*Test[s]*\$"
  ]
}
```

</TabItem>
</Tabs>

### Ignore failures
By default, the build fails if some tests failed. If you want to the build to succeed even if some tests failed use *true*.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
ignoreFailures: true
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  ignoreFailures = true
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  ignoreFailures = true
}
```

</TabItem>
</Tabs>

### Code coverage
Depending on the vendor implementation code coverage may not be supported. By default, code coverage is disabled. If this option is enabled,
code coverage will be collected and marathon assumes that code coverage generation will be setup by user (e.g. proper build flags, jacoco
jar added to classpath, etc).

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
isCodeCoverageEnabled: true
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  isCodeCoverageEnabled = true
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  codeCoverageEnabled = true
}
```

</TabItem>
</Tabs>

### Test output timeout
This parameter specifies the behaviour for the underlying test executor to timeout if there is no output. By default, this is set to 5
minutes.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
testOutputTimeoutMillis: 30000
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  testOutputTimeoutMillis = 30000
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  testOutputTimeoutMillis = 30000
}
```

</TabItem>
</Tabs>

### Test batch timeout

This parameter specifies the behaviour for the underlying test executor to timeout if the batch execution exceeded some duration. By
default, this is set to 30 minutes.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
testBatchTimeoutMillis: 900000
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  testBatchTimeoutMillis = 900000
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  testBatchTimeoutMillis = 900000
}
```

</TabItem>
</Tabs>

### Device provider init timeout

When the test run starts device provider is expected to provide some devices. This should not take more than 3 minutes by default. If your
setup requires this to be changed please override as following:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
deviceInitializationTimeoutMillis: 300000
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  deviceInitializationTimeoutMillis = 300000
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  deviceInitializationTimeoutMillis = 300000
}
```

</TabItem>
</Tabs>

### Analytics tracking
To better understand the use-cases that marathon is used for we're asking you to provide us with anonymised information about your usage. By
default, this is disabled. Use **true** to enable.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
analyticsTracking: true
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  analyticsTracking = true
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  analyticsTracking = true
}
```

</TabItem>
</Tabs>

### Uncompleted test retry quota
By default, tests that don't have any status reported after execution (for example a device disconnected during the execution) retry
indefinitely. You can limit the number of total execution for such cases using this option.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
uncompletedTestRetryQuota: 100
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  uncompletedTestRetryQuota = 100
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  uncompletedTestRetryQuota = 100
}
```

</TabItem>
</Tabs>

### Strict mode
By default, if one of the test retries succeeds then the test is considered successfully executed. If you require success status only when
all retries were executed successfully you can enable the strict mode. This may be useful to verify that flakiness of tests was fixed for
example.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
strictMode: true
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  strictMode = true
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  strictMode = true
}
```

</TabItem>
</Tabs>

### Debug mode
Enabled very verbose logging to stdout of all the marathon components. Very useful for debugging.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
debug: true
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  debug = true
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  debug = true
}
```

</TabItem>
</Tabs>

### Screen recording policy
By default, screen recording will only be pulled for tests that failed (**ON_FAILURE** option). This is to save space and also to reduce the
test duration time since we're not pulling additional files. If you need to save screen recording regardless of the test pass/failure please
use the **ON_ANY** option:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
screenRecordingPolicy: "ON_ANY"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  screenRecordingPolicy = ScreenRecordingPolicy.ON_ANY
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  screenRecordingPolicy = ScreenRecordingPolicy.ON_ANY
}
```

</TabItem>
</Tabs>

### Output configuration
#### Max file path
By default, the max file path for any output file is capped at 255 characters due to some of OSs limitations. This is the reason why some
test runs have lots of "File path length cannot exceed" messages in the log. Since there is currently no API to programmatically 
establish this limit it's user's responsibility to set it up to larger value if OS supports this and the user desires it.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
outputConfiguration:
  maxPath: 1024
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  outputConfiguration {
    maxPath = 1024
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  outputConfiguration {
    maxPath = 1024
  }
}
```

</TabItem>
</Tabs>

[1]: https://github.com/MarathonLabs/marathon/blob/develop/configuration/src/main/kotlin/com/malinskiy/marathon/config/serialization/ConfigurationFactory.kt
