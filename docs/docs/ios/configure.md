---
title: "Configuration"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

To indicate that you're using a vendor config for iOS you have to specify
the *type* in configuration as following:

```yaml
vendorConfiguration:
  type: "iOS"
  additional_option1: ...
  additional_option2: ...
```

:::caution

All the options below should be placed under the `vendorConfiguration` with appropriate yaml indentation

:::

## Required options
### Test bundle
Marathon can run both XCUITests and XCTests. Test bundle requires you to specify application under test as well as test application.
After preprocessing both of these inputs are distilled into an application bundle (e.g. `my.app`) and xctest bundle (e.g. `my-tests.xctest`)
You can specify `.ipa` [application archives][4] as well as `.zip` with the same content as application archive. They will be searched for the
application and xctest bundles. If there are multiple entries matching description - marathon will fail.

#### Raw bundles (.app + .xctest)
```yaml
application: "build/my.app"
testApplication: "build/my.xctest"
```

#### Archive bundles (.ipa/.zip)
:::tip

It is much easier to supply the `.app` application bundle and `.xctest` bundle directly instead of wasting time on packaging a signed application
archive and depending on marathon's runtime type discovery of your bundles

:::
If you want to specify your bundles as `.ipa/.zip`:

```yaml
application: "build/my.ipa"
testApplication: "build/my.zip"
```

#### Derived data dir
```yaml
derivedDataDir: "derivedDataDir/"
```

#### Test type
Marathon will detect if the specified `.xctest` is a XCUITest bundle or XCTest bundle. If you want to save some execution time you
can explicitly specify this:

<Tabs>
<TabItem value="XCUITest" label="XCUITest">

```yaml
testType: "xcuitest"
```

</TabItem>
<TabItem value="XCTest" label="XCTest">

```yaml
testType: "xctest"
```

</TabItem>
</Tabs>

#### Extra applications
Marathon can install additional applications that might be required for testing:

```yaml
extraApplications: 
  - "/path/to/additional.app"
```

### Devices
By default, marathon will look for a file `Marathondevices` in the same folder as `Marathonfile` for the configuration of workers. You can 
override this location with the following property:

```yaml
devices: my/devices.yaml
```

For the documentation of the format of this file refer to [worker's documentation][1]. 

### SSH
:::tip

This section is only required if you're using remote workers and want to provide the same ssh configuration for all of those workers.
You can always specify and/or override this explicitly for each worker

:::
 
For each ssh connection you want to specify authentication, identifying known hosts and keep-alive:

#### Public Key Authentication
To authenticate using private key and a username:

```yaml
ssh:
  authentication:
    type: "publicKey"
    username: "username"
    key: "/home/user/.ssh/id_rsa"
```

#### Password Authentication
To authenticate using username and password:

:::caution

`sshpass` is required to allow rsync to pick up username+password credentials.

:::

```yaml
ssh:
  authentication:
    type: "password"
    username: "username"
    password: "storing-password-here-is-a-bad-idea"
```

:::tip

Storing ssh password in a configuration file directly is a bad idea. Refer to [dynamic configuration][2] and utilize the envvar interpolation
to provide the password for your test runs during runtime.

:::


#### Known hosts
When ssh establishes connection to a remote host it tries to verify the identity of the remote host to mitigate potential men-in-the-middle
attack. You can specify the `known_hosts` file in the OpenSSH format as following:

```yaml
ssh:
  knownHostsPath: "/home/user/.ssh/known_hosts"
```

:::caution

If you omit this configuration then marathon will trust any remote host. This is a bad idea for production.

:::

#### Secure Shell debug
If you are experiencing issues with ssh connections and want to have more information use the following debug flag. Caution: **a lot** of
data is written to stdout when using this flag.

```yaml
ssh:
  debug: true
```

## Optional
### Collecting xcresult
By default, marathon will pull the xcresult bundle into the output folder under device files and cleanup the remote worker to not bloat
the worker storage. To change this override the following:

:::info

As of the time of writing marathon doesn't support merging the xcresult and treats them as just regular file artifacts.

:::

```yaml
  xcresult:
    pull: true
    remoteClean: true
```

#### Attachment lifetime
Marathon generates the xctestrun file for each batch and can specify custom lifecycle attachments. By default, system attachments will be 
deleted on success and user attachments will always be kept in the xcresult, but you can override this:  

```yaml
  xcresult:
    attachments:
      systemAttachmentLifetime: DELETE_ON_SUCCESS
      userAttachmentLifetime: KEEP_ALWAYS
```

Possible values for the lifetime are `KEEP_ALWAYS`, `DELETE_ON_SUCCESS` and `KEEP_NEVER`.

### Screen recorder configuration
By default, marathon will record a h264-encoded video of the internal display with black mask if it is supported. 
If you want to force screenshots or configure the recording parameters you can specify this as follows:

```yaml
screenRecordConfiguration:
  preferableRecorderType: "screenshot"
```

#### Video recorder configuration
Apple's video recorder can encode videos using `codec` `h264` and `hevc`.

:::caution

HEVC encoded videos are not supported by some web browsers. Such videos might not be playable in html reports that marathon produces

:::

```yaml
screenRecordConfiguration:
  videoConfiguration:
    enabled: true
    codec: h264
    display: internal
    mask: black
```

The `display` field can be either `internal` or `external`.
The `mask` field can be either `black` or `ignored`.

#### Screenshot configuration
Marathon can resize and combine screenshots from device into a GIF image

```yaml
screenRecordConfiguration:
  screenshotConfiguration:
    enabled: true
    type: jpeg
    display: internal
    mask: black
    width: 720
    height: 1280
    # ISO_8601 duration
    delay: PT1S
```

The `display` and `mask` fields have the same options as the video recorder.
The `type` specifies the format of a single frame and is advised not to be changes.
The `delay` field specifies the minimal delay between frames using [ISO 8601][3] notation.

### xctestrun environment variables
You specify additional environment variables for your test run:
```yaml
xctestrunEnv:
  MY_ENV_VAR_1: A
  MY_ENV_VAR_2: B
```

These will be placed in the generated xctestrun property list file under the `TestingEnvironmentVariables` key.

:::info

Marathon generates required values for `DYLD_FRAMEWORK_PATH`, `DYLD_LIBRARY_PATH` and `DYLD_INSERT_LIBRARIES` for test environment. 
If you specify custom ones then your values will be placed as a lower priority path elements at the end of the specified envvar.

:::

### xcodebuild test-without-building arguments
You can specify additional arguments to pass to the underlying `xcodebuild test-without-building` invocation.
```yaml
xcodebuildTestArgs:
  "-test-timeouts-enabled": "YES"
  "-maximum-test-execution-time-allowance": "60"
```

It is impossible to override the following reserved arguments:
- `-xctestrun`
- `-enableCodeCoverage`
- `-resultBundlePath`
- `-destination-timeout`
- `-destination`

### Test run lifecycle
Marathon provides two lifecycle hooks: `onPrepare` and `onDispose`. 
For each you can specify one of the following actions: `SHUTDOWN` (shutdown simulator), `ERASE` (erase simulator) and `TERMINATE` (terminate simulator).

These can be useful during provisioning of workers, e.g. you might want to erase the existing simulators before using them

:::warning

If you specify `TERMINATE` marathon will `kill -SIGKILL` the simulators. This usually results in simulators unable to boot with
black screen as well as a number of zombie processes and can only be resolved by erasing the state. In most cases `SHUTDOWN` is the recommended action.

:::

:::tip

If you specify `ERASE` then marathon will first shut down the simulator since it's impossible to erase it otherwise 

:::

An example for a more clean test run:
```yaml
lifecycle:
  onPrepare:
    - ERASE
  onDispose:
    - SHUTDOWN
```

:::tip

Booting simulators is an expensive operation: terminating and erasing simulators is advisable only if you can't accept side effects
from the previous test runs or other usage of simulators

:::

#### Shutdown unused simulators
Marathon will automatically detect if some running simulators are not required by the test run and will shut down them. If you want to 
override this behaviour:

```yaml
lifecycle:
  shutdownUnused: false
```

### Permissions
Marathon can grant permissions to application by bundle id during device setup, e.g.:

```yaml
permissions:
  bundleId: sampleBundle
  grant:
    - contacts
    - photos-add
```

| Permission       | Description                                          |
|------------------|------------------------------------------------------|
| all              | Apply the action to all services                     |
| calendar         | Allow access to calendar                             |
| contacts-limited | Allow access to basic contact info                   |
| contacts         | Allow access to full contact details                 |
| location         | Allow access to location services when app is in use |
| location-always  | Allow access to location services at all times       |
| photos-add       | Allow adding photos to the photo library             |
| photos           | Allow full access to the photo library               |
| media-library    | Allow access to the media library                    |
| microphone       | Allow access to audio input                          |
| motion           | Allow access to motion and fitness data              |
| reminders        | Allow access to reminders                            |
| siri             | Allow use of the app with Siri                       |

### Timeouts
All the timeouts for test run can be overridden, here is an example configuration with default values:

```yaml
timeoutConfiguration:
  # ISO_8601 duration
  shell: PT30S
  shellIdle: PT30S
  reachability: PT5S
  screenshot: PT10S
  video: PT300S
  erase: PT30S
  shutdown: PT30S
  delete: PT30S
  create: PT30S
  boot: PT30S
  install: PT30S
  uninstall: PT30S
  testDestination: PT30S
```

| Name             | Description                                                                                        |
|------------------|----------------------------------------------------------------------------------------------------|
| shell            | Timeout for generic shell commands, unless a more specific action is specified                     |
| shellIdle        | Idle timeout for generic shell commands, any input from stdout/stderr will refresh the time window |
| reachability     | Timeout for considering remote worker unreachable                                                  |
| screenshot       | Timeout for taking a screenshot                                                                    |
| video            | Timeout for recording a video. Should be longer than the duration of your longest test             |
| create           | Timeout for creating a simulator                                                                   |
| boot             | Timeout for booting a simulator                                                                    |
| shutdown         | Timeout for shutting down a simulator                                                              |
| erase            | Timeout for erasing a simulator                                                                    |
| delete           | Timeout for deleting a simulator                                                                   |
| install          | Timeout for installing applications (does not apply for the app bundle or test bundle)             |
| uninstall        | Timeout for uninstalling applications                                                              |
| testDestination  | Timeout for waiting for simulator specified to xcodebuild                                          |

### Threading
Marathon allows you to tweak the number of threads that are used for executing coroutines:

:::tip

This can be important if you're connecting a lot of devices to the test execution, say 100 or a 1000.
Default 8 threads in the devices provider will take a long time to process all of those devices.

:::

```yaml
threadingConfiguration:
  deviceProviderThreads: 8
  deviceThreads: 2
```

`deviceThreads` is the number of threads allocated for processing each device's coroutines. This includes screenshots, parsing results, etc.
It is an advanced setting that should not be changes unless you know what you're doing. A minimal value is 2 for the run to be stable. 

### Hide xcodebuild output
By default, marathon will print the xcodebuild output during testing. You can disable it as following:

```yaml
hideRunnerOutput: true
```

### Compact output
By default, marathon will print the timestamp of each entry on each line.

<Tabs>
<TabItem value="Default" label="Default">

```shell-session
foo@bar $ marathon
D 23:08:45.855 [main] <AppleDeviceProvider> Initializing AppleDeviceProvider
D 23:08:45.879 [AppleDeviceProvider-1] <AppleSimulatorProvider> Establishing communication with ...
D 23:08:46.226 [AppleDeviceProvider-2] <c.m.m.i.c.r.s.s.c.PerformanceDefaultConfig> Available cipher factories: ...
```

</TabItem>
<TabItem value="Compact" label="Compact">

```shell-session
foo@bar $ marathon
D [main] <AppleDeviceProvider> Initializing AppleDeviceProvider
D [AppleDeviceProvider-1] <AppleSimulatorProvider> Establishing communication with ...
D [AppleDeviceProvider-2] <c.m.m.i.c.r.s.s.config.PerformanceDefaultConfig> Available cipher factories: ...
```

</TabItem>
</Tabs>

If you want to make the output more compact by removing the timestamps:

```yaml
compactOutput: true
```

### Remote rsync configuration
:::tip

This section is relevant only if you're using remote workers 

:::

Override rsync binary on the remote worker

```yaml
rsync:
  remotePath: "/usr/bin/rsync-custom"
```

### Clear state between test batch executions
By default, marathon does not clear state between test batch executions. To mitigate potential test side effects, one could add an option to
clear the container data between test runs. Keep in mind that test side effects might still be present.
If you want to isolate tests even further, then you should consider reducing the batch size.

```yaml
dataContainerClear: true
```

### Test parser

:::tip

If you need to parallelize the execution of tests generated at runtime
(i.e. flutter) - xctest parser is your choice.

:::

Test parsing (collecting a list of tests expected to execute) can be done using either binary inspection using nm,
or injecting marathon's proprietary blob and allows marathon to collect a list of tests expected to run without actually running them.

:::note

We don't provide source code for the libxctest-parser module. By using libxctest-parser you're automatically accepting it's [EULA][libxctest-parser-license]

:::

| YAML type |                                                                                                               Pros |                                                                                                      Const |
|-----------|-------------------------------------------------------------------------------------------------------------------:|-----------------------------------------------------------------------------------------------------------:|
| "nm"      |                                                               Doesn't require installation of apps onto the device |                                                      Doesn't support runtime-generated tests, e.g. flutter |
| "xctest"  | Supports precise test parsing and any runtime-generated tests hence allows marathon to parallelize their execution | Requires a booted iOS device for parsing and a fake test run including installation of test app under test |

Default test parser is nm.

<Tabs>
<TabItem value="nm" label="nm">

```yaml
vendorConfiguration:
  type: "iOS"
  testParserConfiguration:
    type: "nm"
    testClassRegexes:
    - "^((?!Abstract).)*Test[s]*$"
```

</TabItem>
<TabItem value="xctest" label="xctest">

```yaml
vendorConfiguration:
  type: "iOS"
  testParserConfiguration:
    type: "xctest"
```

</TabItem>
</Tabs>


[1]: workers.md
[2]: /configuration/dynamic-configuration.md
[3]: https://en.wikipedia.org/wiki/ISO_8601
[libxctest-parser-license]: https://github.com/MarathonLabs/marathon/blob/-/vendor/vendor-ios/src/main/resources/EULA.md
