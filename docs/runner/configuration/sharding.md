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

Executes each test in parallel using any available device in a device pool. This is the default behaviour.

For example, two tests T1, T2 running using default `omni` pool with two devices D1 and D2 will be executed in parallel.

If you have more than one pool, then the parallel sharding happens in each pool independently.
For example, two tests T1, T2 running using device pools P1 (devices D1 and D2) and P2 (devices D3 and D4) will execute tests T1 and T2 in
parallel at least two times, e.g. T1 on D1, T2 on D2, T1 on D3 and T on D4. In short, all tests will run in parallel in each pool.

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

[1]: filtering.md#fragmented-execution-of-tests
