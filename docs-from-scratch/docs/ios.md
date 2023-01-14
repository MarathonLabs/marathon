---
title: "Overview"
---

Executing tests on iOS simulators requires ssh access to
Apple hardware capable of executing tests. Simulators have to be
pre-created when the test run is executed.

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
