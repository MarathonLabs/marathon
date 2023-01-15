---
title: "Retries"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

This is the logic that kicks in if preventive retries facilitated by flakiness configuration have failed.

:::caution

Retries from this configuration are added in-flight, i.e. after the tests were actually executed, hence
there is no way to parallelize test run. This will significantly affect the performance

:::

### No retries

As the name implies, no retries are done. This is the default mode.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
retryStrategy:
  type: "no-retry"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  retryStrategy {}
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  retryStrategy {}
}
```

</TabItem>
</Tabs>

### Fixed quota retry strategy

Parameter **totalAllowedRetryQuota** below specifies how many retries at all (for all the tests is total) are allowed. 

Parameter **retryPerTestQuota** controls how many retries can be done for each test individually.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
retryStrategy:
  type: "fixed-quota"
  totalAllowedRetryQuota: 100
  retryPerTestQuota: 3
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  retryStrategy {
    fixedQuota {
      retryPerTestQuota = 3
      totalAllowedRetryQuota = 100
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  retryStrategy {
    fixedQuota {
      retryPerTestQuota = 3
      totalAllowedRetryQuota = 100
    }
  }
}
```

</TabItem>
</Tabs>
