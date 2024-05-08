---
title: "Analytics"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Configuration of analytics backend to be used for storing and retrieving test metrics. This plays a major part in optimising performance and
mitigating flakiness.

### Disable analytics

By default, no analytics backend is expected which means that each test will be treated as a completely new test.

### [InfluxDB v2][3]

Assuming you've done the setup for InfluxDB v2 you need to provide:

- url
- token - Token for authentication
- organization - Organization is the name of the organization you wish to write/read from
- bucket - Destination bucket to write/read from
- retention policy

Bucket is quite useful in case you have multiple configurations of tests/devices and you don't want metrics from one configuration to
affect the other one, e.g. regular and end-to-end tests.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
analyticsConfiguration:
  type: "influxdb2"
  url: "http://influx2.svc.cluster.local:8086"
  token: "my-super-secret-token"
  organization: "starlabs"
  bucket: "marathon"
  retentionPolicyConfiguration:
    everySeconds: 604800  # Duration in seconds for how long data will be kept in the database. 0 means infinite. minimum: 0
    shardGroupDurationSeconds: 0 # Shard duration measured in seconds
  defaults:
    successRate: 0.1
    duration: "PT300S"
  readOnly: false
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
import java.time.Duration

marathon {
  analytics {
    influx {
      url = "http://influx2.svc.cluster.local:8086"
      token = "my-super-secret-token"
      organization = "starlabs"
      bucket = "marathon"
      defaults = Defaults(0.0, Duration.ofMinutes(5))
      readOnly = false
    }
  }
}
```

</TabItem>
</Tabs>

### [InfluxDB][1]

Assuming you've done the setup for InfluxDB you need to provide:

- url
- username
- password
- database name
- retention policy

Database name is quite useful in case you have multiple configurations of tests/devices and you don't want metrics from one configuration to
affect the other one, e.g. regular and end-to-end tests.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
analyticsConfiguration:
  type: "influxdb"
  url: "http://influx.svc.cluster.local:8086"
  user: "root"
  password: "root"
  dbName: "marathon"
  retentionPolicyConfiguration:
    name: "rpMarathonTest"
    duration: "90d"
    shardDuration: "1h"
    replicationFactor: 5
    isDefault: false
  defaults:
    successRate: 0.1
    duration: "PT300S"
  readOnly: false
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  analytics {
    influx {
      url = "http://influx.svc.cluster.local:8086"
      user = "root"
      password = "root"
      dbName = "marathon"
      defaults = Defaults(0.0, Duration.ofMinutes(5))
      readOnly = false
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  analytics {
    influx {
      url = "http://influx.svc.cluster.local:8086"
      user = "root"
      password = "root"
      dbName = "marathon"
      defaults = Defaults(0.0, Duration.ofMinutes(5))
      readOnly = false
    }
  }
}
```

</TabItem>
</Tabs>

### [Graphite][2]

Graphite can be used as an alternative to InfluxDB. It uses the following parameters:

- host
- port (optional) - the default is 2003
- prefix (optional) - no metrics prefix will be used if not specified

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
analyticsConfiguration:
  type: "graphite"
  host: "influx.svc.cluster.local"
  port: "8080"
  prefix: "prf"
  defaults:
    successRate: 0.1
    duration: "PT300S"
  readOnly: false
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  analytics {
    graphite {
      host = "influx.svc.cluster.local"
      port = "8080"
      prefix = "prf"
      defaults = Defaults(0.0, Duration.ofMinutes(5))
      readOnly = false
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  analytics {
    graphite {
      host = "influx.svc.cluster.local"
      port = "8080"
      prefix = "prf"
      defaults = Defaults(0.0, Duration.ofMinutes(5))
      readOnly = false
    }
  }
}
```

</TabItem>
</Tabs>

[1]: https://www.influxdata.com/
[2]: https://graphiteapp.org/
[3]: https://docs.influxdata.com/influxdb/v2.0/
