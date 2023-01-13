# Overview
In order to execute tests on iOS simulators marathon requires ssh access to
Apple hardware capable of executing tests. Simulators have to be already
pre-created when the test run is executed.

# Providing devices
Currently iOS device provider reads the available devices from the
**Marathondevices** file which has the following format:

```yaml
- host: "10.0.0.1"
  udid: "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
- host: "10.0.0.2"
  udid: "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
```

This is basically a list of simulator UDID's with the IP of the MacOS system.

Logging into the machine is done using private key authentication which is
specified using vendor specific options.

# Vendor specific options
To indicate that you're using a vendor config for android you have to specify
the *type* in configuration as following:

```yaml
vendorConfiguration:
  type: "iOS"
  additional_option1: ...
  additional_option2: ...
```

# Required options
## Derived data path
Specify your derived data path in order to execute the tests

```yaml
derivedDataDir: "derived-data"
```

## Source code root
In order to get the list of tests marathon parses your Swift source code and
finds all test classes and methods based on regex specified in the core
configuration.

```yaml
sourceRoot: "sample-appUITests"
```

## Authentication
In order to authenticate using ssh you have to provide a private key and a
username as following:

```yaml
remoteUsername: "username"
remotePrivateKey: "/home/user/.ssh/id_rsa"
```

# Optional
## xctestrun path
Force a specific xctestrun to be used

```yaml
xctestrunPath: "a/Build/Products/UITesting_iphonesimulator11.0-x86_64.xctestrun"
```

## Remote rsync path
Specify rsync binary on the remote machine manually

```yaml
remoteRsyncPath: "/usr/bin/rsync"
```

## Debug ssh connection
Use this option for a very verbose debug output of ssh transport layer

```yaml
debugSsh: true
```

## Custom Marathondevices path
If your Marathondevices file is not in the project root then you can override
this here

```yaml
devicesFile: "/opt/Marathondevices"
```

## Collecting xcresult
By default, marathon will pull the xcresult bundle into the output folder under device files and cleanup the remote worker to not bloat
the worker storage. To change this override the following:

```yaml
vendorConfiguration:
  type: "iOS"
  xcresult:
    pull: true
    remoteClean: true
```

As of the time of writing marathon doesn't support merging the xcresult and treats them as just regular file artifacts.
