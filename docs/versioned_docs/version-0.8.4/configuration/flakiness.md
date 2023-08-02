---
title: "Flakiness"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

This is the code of the prediction logic for marathon. Using the analytics backend we can understand the success rate and hence queue preventive
retries to mitigate the flakiness of the tests and test environment.

### Ignore flakiness

Nothing is done preventatively in this mode. This is the default behaviour.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
flakinessStrategy:
  type: "ignore"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  flakinessStrategy {}
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  flakinessStrategy {}
}
```

</TabItem>
</Tabs>

### Probability based flakiness strategy

The main idea is that flakiness strategy anticipates the flakiness of the test based on the probability of test passing and tries to
maximise the probability of passing when executed multiple times. 

For example the probability of test A passing is 0.5 and configuration has
probability of 0.8 requested, then the flakiness strategy multiplies the test A to be executed 3 times (0.5 x 0.5 x 0.5 = 0.125 is the
probability of all tests failing, so with probability 0.875 > 0.8 at least one of tests will pass).

$$
P_{passing-with-retries} = 1 - P_{passing}^N = 1 - 0.5^3 = 0.875
$$

The minimal probability that you're comfortable with is specified using **minSuccessRate** during the time window controlled by the **timeLimit**.
Additionally, if you specify too high **minSuccessRate** you'll have too many retries, so the upper bound for this is controlled by the
**maxCount** parameter so that this strategy will calculate the required number of retries according to the **minSuccessRate** but if it's
higher than the **maxCount** it will choose **maxCount**.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
flakinessStrategy:
  type: "probability"
  minSuccessRate: 0.7
  maxCount: 3
  timeLimit: "2015-03-14T09:26:53.590Z"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  flakinessStrategy {
    probabilityBased {
      minSuccessRate = 0.7
      maxCount = 3
      timeLimit = Instant.now().minus(Duration.parse("PT1H"))
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  flakinessStrategy {
    probabilityBased {
      minSuccessRate = 0.7
      maxCount = 3
      timeLimit = Instant.now().minus(Duration.parse("PT1H"))
    }
  }
}
```

</TabItem>
</Tabs>
