import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Maestro

### Supported platforms
Marathon Cloud supports Maestro for iOS and Android.

**Important Notes on Test execution**:
- all file paths within your Maestro flows must use Linux-style syntax (e.g., path/to/file.yaml)
- each Maestro flow is treated as a separate test by Marathon Cloud
- sequential flow logic is not supported, as it conflicts with Marathon's design to complete all tests in under 15 minutes.


To execute your tests, use the command below:
<Tabs groupId="operating-systems">
<TabItem value="iOS" label="iOS">

```shell
export MARATHON_CLOUD_API_KEY=generated_api_key
marathon-cloud run maestro ios --application your_application.app --test-application ./path/to/tests folder_or_file_to_execute
```
</TabItem>
<TabItem value="Android" label="Android">

```shell
export MARATHON_CLOUD_API_KEY=generated_api_key
marathon-cloud run maestro android --application your_app.apk --test-application ./path/to/tests folder_or_file_to_execute 

```
</TabItem>
</Tabs>

Here's a breakdown of the variables and flags used in the example:
- `./path/to/tests` - the root directory containing all your Maestro test files
- `folder_or_file_to_execute` - a space-separated list of specific folders and files you want to run within the test directory. 
**Note:** Marathon Cloud does not execute tests from subfolders when a folder is specified. To change this behaviour 
use [Maestro inclusion patterns](https://docs.maestro.dev/cli/test-suites-and-reports#inclusion-patterns)
- `--application your_application.app` - path to the [iOS application](/intro/ios#application-and-test-application)
- `--application your_app.apk` - path to the [Android application](/intro/android#application-and-test-application)

### Maestro environment variables
Maestro tests often require environment variables to handle
dynamic data like usernames, API keys or appId. To pass these variables to your tests on Marathon Cloud, you use the **--maestro-env** flag.

To pass a custom environment variable, reference it within your Maestro test using the ${VARIABLE\_NAME} syntax. 
All variables must start with MAESTRO_.
For example, to use a username variable:
```shell
appId: your.app.id
---
- launchApp
- inputText: ${MAESTRO_USERNAME}
```

You pass variables as key-value pairs (KEY=VALUE). For instance, to set MAESTRO_USERNAME to MY_USER_NAME:
```shell
export MARATHON_CLOUD_API_KEY=generated_api_key
marathon-cloud run maestro android --application your_app.apk --test-application ./path/to/tests --maestro-env=MAESTRO_USERNAME=MY_USER_NAME folder_or_file_to_execute 

```
**Important:**
When you need to reference the application ID (appId) within a Maestro test, you must use a special variable name.
If your test flow requires the **APP_ID** to be a variable:
```shell
# flow.yaml

appId: ${APP_ID}
---
- launchApp
- tapOn: "Text on the screen"
```
You must pass the application ID using the **MAESTRO_APP_ID** environment variable.
```shell
export MARATHON_CLOUD_API_KEY=generated_api_key
marathon-cloud run maestro android --application your_app.apk --test-application ./path/to/tests --maestro-env=MAESTRO_APP_ID=your.app.id folder_or_file_to_execute 

```
This specific handling for appId is required to ensure predictable behavior and prevent unexpected test failures.
We invite you to leave a comment [here](https://github.com/mobile-dev-inc/Maestro/issues/1789)
 if you agree that Maestro should support MAESTRO_APP_ID for the app ID.

### Maestro filtering
For more granular control over which tests run, you can use Maestro's tag-based filtering in addition to the recommended practice of organizing tests into folders.
This filtering feature lets you define which tests to include (allowlist) and which to exclude (blocklist) using specific tags.

create a YAML file (e.g., filter.yaml) that specifies your filtering configuration. The example below shows how to define tags to allow or block:
```shell
filteringConfiguration:
  allowlist:
    - type: "annotation"
      values:
        - "tag1"
        - "tag2"
  blocklist:
    - type: "annotation"
      values:
        - "tag3"
        - "tag4"
```
Next, pass this filter file to your Marathon Cloud command using the **--filter-file** flag. Here is how your full command would look:
```shell
export MARATHON_CLOUD_API_KEY=generated_api_key
marathon-cloud run maestro android --application your_app.apk --test-application ./path/to/tests --filter-file filter.yaml folder_or_file_to_execute 
```
### Best practices
To ensure optimal performance and reliability of your test suite, adhere to these structural recommendations when using Marathon Cloud and Maestro.

**Key Recommendations**
- **Test Duration**: Keep individual test flows short. 
We recommend a duration of 40 to 80 seconds, and no more than 3 minutes. This practice improves execution speed and simplifies debugging.
- **Test Independence**: Each test flow should be self-contained and capable of running on a fresh device. 
If a test requires a specific state, such as a user login, include the setup steps at the beginning of that test's flow.
- **Enable Parallelism**: Marathon Cloud runs multiple tests concurrently. 
To leverage this feature, structure your test suite by placing each flow in a separate, independent YAML file. Flows within a single file are executed sequentially as one test and will not be parallelized.


