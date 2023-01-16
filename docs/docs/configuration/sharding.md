---
title: "Sharding"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Sharding is a mechanism that allows the marathon to affect the tests scheduled for execution inside each pool.

:::caution

Sharding in marathon is NOT related to splitting the tests into parallel runs. 
If you're looking for parallelization of marathon runs - check out the [fragmentation filter][1]

:::

### Parallel sharding

Executes each test using available devices. This is the default behaviour.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
shardingStrategy:
  type: "parallel"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  shardingStrategy {}
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  shardingStrategy {}
}
```

</TabItem>
</Tabs>

### Count sharding

Executes each test **count** times inside each pool. For example you want to test the flakiness of a specific test hence you need to execute
this test a lot of times. Instead of running the build X times just use this sharding strategy and the test will be executed X times.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
shardingStrategy:
  type: "count"
  count: 5
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  shardingStrategy {
    countSharding {
      count = 5
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  shardingStrategy {
    countSharding {
      count = 5
    }
  }
}
```

</TabItem>
</Tabs>

[1]: ./filtering#fragmented-execution-of-tests
