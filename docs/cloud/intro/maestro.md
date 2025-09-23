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

### Command Breakdown
Here's a breakdown of the variables and flags used in the example:
- ./path/to/tests - the root directory containing all your Maestro test files
- folder_or_file_to_execute - a space-separated list of specific folders and files you want to run within the test directory. **Note:** Marathon Cloud does not execute tests from subfolders when a folder is specified.
For iOS:
- --application your_application.app - path to the [iOS application](/intro/ios#application-and-test-application)
For Android:
- --application your_app.apk - path to the [Android application](/intro/android#application-and-test-application)

### Maestro environment variables
If your Maestro tests rely on environment variables, you'll need to pass them to Marathon Cloud. 
A common use case is referencing the application ID in a test flow, as shown in this example:
```shell
# flow.yaml

appId: ${APP_ID}
---
- launchApp
- tapOn: "Text on the screen"
```
To provide the value for **APP_ID**, use the **--maestro-env** flag. For instance, to set **APP_ID** to **your.app.id**, you would execute the following command:
```shell
export MARATHON_CLOUD_API_KEY=generated_api_key
marathon-cloud run maestro android --application your_app.apk --test-application ./path/to/tests --maestro-env=APP_ID=your.app.id folder_or_file_to_execute 

```

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
