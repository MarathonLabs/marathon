---
title: "Configuration"
---

To indicate that you're using a vendor config for iOS you have to specify
the *type* in configuration as following:

```yaml
vendorConfiguration:
  type: "iOS"
  additional_option1: ...
  additional_option2: ...
```

## Required options
### Derived data path
Specify your derived data path to execute the tests

```yaml
derivedDataDir: "derived-data"
```

## Source code root
To get the list of tests marathon parses your Swift source code and
finds all test classes and methods based on regex specified in the core
configuration.

```yaml
sourceRoot: "sample-appUITests"
```

## Authentication
To authenticate using ssh you have to provide a private key and a
username as following:

```yaml
remoteUsername: "username"
remotePrivateKey: "/home/user/.ssh/id_rsa"
```

## Optional
### xctestrun path
Force a specific xctestrun to be used

```yaml
xctestrunPath: "a/Build/Products/UITesting_iphonesimulator11.0-x86_64.xctestrun"
```

### Remote rsync path
Specify rsync binary on the remote machine manually

```yaml
remoteRsyncPath: "/usr/bin/rsync"
```

### Debug ssh connection
Use this option for a very verbose debug output of ssh transport layer

```yaml
debugSsh: true
```

### Custom Marathondevices path
If your Marathondevices file is not in the project root then you can override
this here

```yaml
devicesFile: "/opt/Marathondevices"
```

### Collecting xcresult
By default, marathon will pull the xcresult bundle into the output folder under device files and cleanup the remote worker to not bloat
the worker storage. To change this override the following:

:::info

As of the time of writing marathon doesn't support merging the xcresult and treats them as just regular file artifacts.

:::

```yaml
vendorConfiguration:
  type: "iOS"
  xcresult:
    pull: true
    remoteClean: true
```
