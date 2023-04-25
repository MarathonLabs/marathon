---
title: "Batching"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Batching mechanism allows you to trade off stability for performance. A sorted group of tests without duplicates, executed using a single command is called a **batch**. 
Most of the time, this means that between tests in the same batch you're sharing the device state and code state increasing the risk of side-effect because there is no external clean-up. 
On the other hand you gain performance improvements since the execution command is usually quite slow (up to 10 seconds for some platforms). Most importantly, batching allows tests to be horizontally parallelized.

### Isolate batching

Each test is executed using separate command execution, so performance is sacrificed for stability.
This is the default mode.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
batchingStrategy:
  type: "isolate"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  batchingStrategy {}
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  batchingStrategy {}
}
```

</TabItem>
</Tabs>

### Fixed size batching

Each batch is created based on the **size** parameter which is required. When a new batch of tests is needed the queue is dequeued for at
most **size** tests.

Optionally if you want to limit the batch duration you have to specify the **timeLimit** for the test metrics time window and the **
durationMillis**. For each test the analytics backend is accessed and **percentile** of it's duration is queried. If the sum of durations is
more than the **durationMillis** then no more tests are added to the batch.

This is useful if you have extremely long tests and you use batching, e.g. you batch by size 10 and your test run duration is roughly 10
minutes, but you have tests that are expected to run 2 minutes each. If you batch all of them together then at least one device will be
finishing it's execution in 20 minutes while all other devices might already finish. To mitigate this just specify the time limit for the
batch using **durationMillis**.

Another optional parameter for this strategy is the **lastMileLength**. At the end of execution batching tests actually hurts the
performance so for the last tests it's much better to execute them in parallel in separate batches. This works only if you execute on
multiple devices. You can specify when this optimisation kicks in using the **lastMileLength** parameter, the last **lastMileLength** tests
will use this optimisation.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
batchingStrategy:
  type: "fixed-size"
  size: 5
  durationMillis: 100000
  percentile: 80.0
  timeLimit: "-PT1H"
  lastMileLength: 10
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  batchingStrategy {
    fixedSize {
      size = 5
      durationMillis = 100000
      percentile = 80.0
      timeLimit = Instant.now().minus(Duration.parse("PT1H"))
      lastMileLength = 10
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  batchingStrategy {
    fixedSize {
      size = 5
      durationMillis = 100000
      percentile = 80.0
      timeLimit = Instant.now().minus(Duration.parse("PT1H"))
      lastMileLength = 10
    }
  }
}
```

</TabItem>
</Tabs>


### Test class batching

Each batch will be based on test class size. We can advice to use this configuration wisely to avoid cross runs side effects. 

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
batchingStrategy:
  type: "class-name"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
    batchingStrategy {
        className {}
    }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  batchingStrategy {
    className {}
  }
}
```

</TabItem>
</Tabs>
