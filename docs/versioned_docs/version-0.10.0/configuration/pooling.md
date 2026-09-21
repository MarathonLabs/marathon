---
title: "Pooling"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Pooling strategy defines how devices are grouped together.

## Omni a.k.a. one huge pool

All connected devices are merged into one group. **This is the default mode**.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
poolingStrategy:
  type: "omni"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  poolingStrategy {}
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  poolingStrategy {}
}
```

</TabItem>
</Tabs>

## By abi

Devices are grouped by their ABI, e.g. *x86* and *mips*.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
poolingStrategy:
  type: "abi"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  poolingStrategy {
    abi = true
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  poolingStrategy {
    abi = true
  }
}
```

</TabItem>
</Tabs>

## By manufacturer

Devices are grouped by manufacturer, e.g. *Samsung* and *Yota*.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
poolingStrategy:
  type: "manufacturer"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  poolingStrategy {
    manufacturer = true
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  poolingStrategy {
    manufacturer = true
  }
}
```

</TabItem>
</Tabs>

## By device model

Devices are grouped by model name, e.g. *LG-D855* and *SM-N950F*.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
poolingStrategy:
  type: "device-model"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  poolingStrategy {
    model = true
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  poolingStrategy {
    model = true
  }
}
```

</TabItem>
</Tabs>

## By OS version

Devices are grouped by OS version, e.g. *24* and *25*.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
poolingStrategy:
  type: "os-version"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  poolingStrategy {
    operatingSystem = true
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  poolingStrategy {
    operatingSystem = true
  }
}
```

</TabItem>
</Tabs>
