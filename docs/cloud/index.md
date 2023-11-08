import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Quick start

### Install
The installation can be performed using [Homebrew](https://brew.sh/). Here’s how to add the MarathonLabs repository:
```shell
brew tap malinskiy/tap
```
Next, install the Marathon Cloud CLI:

```shell
brew install malinskiy/tap/marathon-cloud
```
Alternatively, you can download prebuild binaries for Windows, Linux, or MacOS from [the Release page](https://github.com/MarathonLabs/marathon-cloud-cli/releases).


### API Key

Token creation and management are available at the [Tokens page](https://cloud.marathonlabs.io/tokens). Generate a token and save it somewhere safe for the next step.

### Samples (optional)

To showcase the advantages of Marathon Cloud compared to other solutions, we’ve prepared a sample app with 300 tests, out of which 15% are flaky. During the initial run, our platform will gather information about the tests. During the second run, it will optimize it to fit within 15 minutes.

<Tabs groupId="operating-systems" >
<TabItem value="iOS" label="iOS" className="tab-content-with-text">
Download prebuild iOS Application

```shell
curl https://cloud.marathonlabs.io/samples/ios/sample.zip -o sample.zip
```
Download prebuild iOS Test Application

```shell
curl https://cloud.marathonlabs.io/samples/ios/sampleUITests-Runner.zip -o sampleUITests-Runner.zip
```

</TabItem>
<TabItem value="Android" label="Android" className="tab-content-with-text" >
Download prebuild Android Application

```shell
curl https://cloud.marathonlabs.io/samples/android/app.apk -o app.apk
```

Download prebuild Android Test Application
```shell
curl https://cloud.marathonlabs.io/samples/android/appTest.apk -o appTest.apk
```

</TabItem>
</Tabs>

To use your own applications please read Documentation.


### Execution

Now you can start running your tests. Use the following command to execute the CLI with the necessary parameters:

<Tabs groupId="operating-systems">
<TabItem value="iOS" label="iOS">

```shell
marathon-cloud -api_key generated_api_key -apk sample.zip -testapk sampleUITests-Runner.zip -platform iOS
```
</TabItem>
<TabItem value="Android" label="Android">

```shell
marathon-cloud -api_key api_key -apk app.apk -testapk appTest.apk -platform Android
```
</TabItem>
</Tabs>

For additional parameters, refer to the [marathon-cloud-cli README](https://github.com/MarathonLabs/marathon-cloud-cli/#installation)

