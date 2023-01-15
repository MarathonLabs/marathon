---
title: "Dynamic configuration"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Marathon allows you to pass dynamic variables to your marathon configuration, e.g. a list of tests or url for analytics backend.

## CLI

Marathonfile support environment variable interpolation in the Marathonfile. Every occurance of `${X}` in the Marathonfile will be replaced 
with the value of envvar `X` For example, if you want to dynamically pass the index of the test run to the fragmentation filter:

```yaml
filteringConfiguration:
  allowlist:
    - type: "fragmentation"
      index: ${MARATHON_FRAGMENT_INDEX}
      count: 10
```

and then execute the testing as following:

```shell-session
foo@bar:~$ MARATHON_FRAGMENT_INDEX=0 marathon
```

## Gradle

To pass a parameter to the Gradle's build script:

```shell-session
foo@bar:~$ gradle -PmarathonOutputDir=reports
```

<Tabs>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  output = property("marathonOutputDir")
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  output = property('marathonOutputDir')
}
```

</TabItem>
</Tabs>

For more info refer to the [Gradle's dynamic project properties](https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#properties)
