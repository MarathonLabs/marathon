---
title: "Overview"
---

Executing tests on iOS simulators requires access to
Apple hardware capable of executing tests. 

## Providing Apple hardware
To inform marathon of the accessible Apple hardware a yaml file named **Marathondevices**
is read on startup. 

The structure of the file is a workers object with list of worker machines and the simulator devices
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

This might be all that you need for getting started with marathon, but if you want to take
your test run performance to the next level - keep reading for the ability to parallelise your test runs across
hundreds of simulators

:::

### SSH worker
If you want to connect to a remote Apple hardware (maybe because you need to parallelize across 5 MacMinis or 
because you're executing the tests from a Linux machine in CI), then you can use ssh:

```yaml
workers:
  - transport:
      type: ssh
      addr: 10.0.0.2
    devices:
      - type: simulatorProfile
        deviceType: com.apple.CoreSimulator.SimDeviceType.iPhone-13-mini
```

## Providing devices
iOS device provider reads the available devices from the
**Marathondevices** file which has the following format:

```yaml
- host: "10.0.0.1"
  udid: "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
- host: "10.0.0.2"
  udid: "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
```

This is basically a list of simulator UDID's with the IP of the macOS system.

Logging into the machine is done using private key authentication which is
specified using vendor specific options.
