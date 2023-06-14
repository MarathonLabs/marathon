---
title: "Sorting"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

## Sorting strategy

Prioritising performance of test execution requires tests to be sorted. Sorting is possible only when analytics backend is available.

Sorting can be done based on test duration and success/failure rate.

### No sorting

No sorting of tests is done at all. This is the default behaviour.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
sortingStrategy:
  type: "no-sorting"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  sortingStrategy {}
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  sortingStrategy {}
}
```

</TabItem>
</Tabs>

### Success rate sorting

For each test analytics storage is providing the success rate for a time window specified by time **timeLimit** parameter. All the tests are
then sorted by the success rate in an increasing order, that is failing tests go first and successful tests go last. If you want to reverse
the order set the `ascending` to `true`.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
sortingStrategy:
  type: "success-rate"
  timeLimit: "2015-03-14T09:26:53.590Z"
  ascending: false
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  sortingStrategy {
    successRate {
      limit = Instant.now().minus(Duration.parse("PT1H"))
      ascending = false
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  sortingStrategy {
    successRate {
      limit = Instant.now().minus(Duration.parse("PT1H"))
      ascending = false
    }
  }
}
```

</TabItem>
</Tabs>

### Execution time sorting

For each test analytics storage is providing the X percentile duration for a time window specified by time **timeLimit** parameter. Apart
from absolute date/time it can be also be an ISO 8601 formatted duration.

Percentile is configurable via the **percentile** parameter.

All the tests are sorted so that long tests go first and short tests are executed last. This allows marathon to minimise the error of
balancing the execution of tests at the end of execution.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
sortingStrategy:
  type: "execution-time"
  percentile: 80.0
  timeLimit: "-PT1H"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  sortingStrategy {
    executionTime {
      percentile = 80.0
      timeLimit = Instant.now().minus(Duration.parse("PT1H"))
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  sortingStrategy {
    executionTime {
      percentile = 80.0
      timeLimit = Instant.now().minus(Duration.parse("PT1H"))
    }
  }
}
```

</TabItem>
</Tabs>

### Random order sorting

Sort tests in random order. This strategy may be useful to detect dependencies between tests.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
sortingStrategy:
  type: "random-order"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  sortingStrategy {
    randomOrder {
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  sortingStrategy {
    randomOrder {
    }
  }
}
```

</TabItem>
</Tabs>
