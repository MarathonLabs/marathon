---
title: "Workers"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

## Providing Apple workers
To inform marathon of the accessible Apple hardware a declarative yaml file named **Marathondevices**
is read on startup.

The structure of the file is a workers object with a list of worker machines and real or simulated devices
that can be used or created on those workers.

```yaml
workers:
  - transport:
      type: ssh
      addr: 10.0.0.2
    devices:
      - type: simulator
        udid: "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
  - transport:
      type: ssh
      addr: 10.0.0.3  
    devices:
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
```

For each worker a transport object describes how to access this particular worker

### Local worker
If you're already running marathon on Apple hardware then you can use it in your test runs.

```yaml
workers:
  - transport:
      type: local
    devices:
      - type: simulator
        udid: "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
```

:::tip

This might be all that you need for getting started with marathon in terms of providing hardware, but if you want to take
your test run performance to the next level - keep reading for the ability to parallelize your test runs across
hundreds of simulators

:::

### SSH worker
If you want to connect to remote Apple hardware (maybe because you need to parallelize across 5 MacMinis or
because you're executing tests from a Linux machine in CI), then you can use ssh:

```yaml
workers:
  - transport:
      type: ssh
      addr: 10.0.0.2
    devices:
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
```

Ssh transport accepts three parameters:
1. **addr** - address of the host
2. **port** - port of the ssh server, defaults to 22
3. **authentication** - override for authentication specifically for this worker
4. **checkReachability** - optional flag for enabling/disabling ICMP-based ping that might not work in certain network environments

## Providing simulator devices
Each worker definition has a list of devices that can be used on that worker

:::caution

Using any devices assumes you're responsible for pre-provisioning appropriate devices or specifying correct versions of marathon-provisioned
devices that will work for your application

:::

### simulator
This device type is a pre-provisioned Simulator identified using UDID (Unique Device Identifier).

```yaml
workers:
  - transport:
      type: local
    devices:
      - type: simulator
        udid: "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
```

### simulatorProfile
This device type assumes you just want some instance of a simulator with a specified type, e.g.:

```yaml
workers:
  - transport:
      type: ssh
      addr: 10.0.0.2
    devices:
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
```

:::tip

You can list available device type identifiers using the following command:
```shell-session
foo@bar $ xcrun simctl list devicetypes
== Device Types ==
iPhone 4s (com.apple.CoreSimulator.SimDeviceType.iPhone-4s)
iPhone 5 (com.apple.CoreSimulator.SimDeviceType.iPhone-5)
iPhone 5s (com.apple.CoreSimulator.SimDeviceType.iPhone-5s)
...
```

:::

When the test run starts marathon will analyze if a device of a requested deviceType has already been created.
If it exists then marathon will reuse it for testing. If it doesn't exist it will create a new simulator.

#### newNamePrefix
By default, newly created devices will have a prefix **marathon**. You can override it if you need to:
```yaml
workers:
  - transport:
      type: ssh
      addr: 10.0.0.2
    devices:
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
        newNamePrefix: red-pill
```

#### runtime
By default, marathon will use the latest available runtime in the current active Xcode. If you want to explicitly
specify the runtime version:

```yaml
workers:
  - transport:
      type: ssh
      addr: 10.0.0.2
    devices:
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
        runtime: 
```

:::tip

You can list available device type identifiers using the following command:
```shell-session
foo@bar $ xcrun simctl list runtimes -v
== Runtimes ==
iOS 16.2 (16.2 - 20C52) - com.apple.CoreSimulator.SimRuntime.iOS-16-2 [/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Library/Developer/CoreSimulator/Profiles/Runtimes/iOS.simruntime]
tvOS 16.1 (16.1 - 20K67) - com.apple.CoreSimulator.SimRuntime.tvOS-16-1 [/Library/Developer/CoreSimulator/Volumes/tvOS_20K67/Library/Developer/CoreSimulator/Profiles/Runtimes/tvOS 16.1.simruntime]
watchOS 9.1 (9.1 - 20S75) - com.apple.CoreSimulator.SimRuntime.watchOS-9-1 [/Library/Developer/CoreSimulator/Volumes/watchOS_20S75/Library/Developer/CoreSimulator/Profiles/Runtimes/watchOS 9.1.simruntime]
...
```

:::

:::caution

Most installations of Xcode will only have one version of the runtime available, so specifying the runtime version
explicitly will break for those installations on update, because the runtime will not be available on update
by default.

:::

## Providing macOS desktop device
Testing macOS desktop apps requires access to macOS instances. Depending on the environment of marathon run execution you can use local
instance, remote one or a mix of both:

<Tabs>
<TabItem value="local" label="local">

```yaml
workers:
  - transport:
      type: local
    devices:
      - type: host
```

</TabItem>
<TabItem value="remote" label="remote">

```yaml
workers:
  - transport:
      type: ssh
      addr: 10.0.0.2
    devices:
      - type: host
  - transport:
      type: ssh
      addr: 10.0.0.3
    devices:
      - type: host
```

</TabItem>
<TabItem value="hybrid" label="hybrid">

```yaml
workers:
  - transport:
      type: ssh
      addr: 10.0.0.2
    devices:
      - type: host
  - transport:
      type: local
    devices:
      - type: host
```

</TabItem>
</Tabs>
