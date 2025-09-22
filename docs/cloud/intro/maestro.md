import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Maestro

### Supported platforms
Marathon Cloud supports Maestro for iOS and Android.

To execute your tests, use the command below:
<Tabs groupId="operating-systems">
<TabItem value="iOS" label="iOS">

```shell
export MARATHON_CLOUD_API_KEY=generated_api_key
marathon-cloud run maestro ios -a your_application.app -t ./path/to/tests --maestro-env=MAESTRO_APP_ID=your.app.id folder_or_file_to_execute
```
</TabItem>
<TabItem value="Android" label="Android">

```shell
export MARATHON_CLOUD_API_KEY=generated_api_key
marathon-cloud run maestro android -a your_app.apk -t ./path/to/tests --maestro-env=MAESTRO_APP_ID=your.app.id folder_or_file_to_execute 

```
</TabItem>
</Tabs>

### Command Breakdown
Here's a breakdown of the variables and flags used in the example:
- ./path/to/tests - the root directory containing all your Maestro test files
- folder_or_file_to_execute - a space-separated list of specific folders and files you want to run within the test directory. **Note:** Marathon Cloud does not execute tests from subfolders when a folder is specified.
For iOS:
- -a your_application.app - path to the [iOS application](/intro/ios#application-and-test-application)
For Android:
- -a your_app.apk - path to the [Android application](/intro/android#application-and-test-application)
- --maestro-env=MAESTRO_APP_ID=your.app.id - This optional flag allows you to pass environment variables to your Maestro tests. It is required if your test flow uses a variable like appId: ${MAESTRO_APP_ID}. Replace your.app.id with the actual bundle ID for your application.


