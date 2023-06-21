import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Introduction

[Marathon Cloud](marathonlabs.io) revolutionizes your app testing experience. The platform provides infinite virtual devices and will automatically shard, sort, distribute, and retry your tests, allowing all tests to be completed in **a maximum of 15 minutes**. First **50 hours** are free!

## Getting started

### Install

The installation can be performed using [Homebrew](https://brew.sh/). Here's how to add the MarathonLabs repository:

```shell
brew tap malinskiy/tap
```
Next, install the Marathon Cloud CLI:
```shell
brew install malinskiy/tap/marathon-cloud
```
Alternatively, you can download prebuilt binaries for Windows, Linux, or MacOS from [the Release page](https://github.com/MarathonLabs/marathon-cloud-cli/releases).

### API Key

Token creation and management are available at [the Tokens page](https://dev.testwise.pro/tokens). Generate a token and save it somewhere safe for the next step.

### Samples (optional)

To showcase the advantages of Marathon Cloud compared to other solutions, we've prepared a sample app with 300 tests, out of which 15% are flaky. During the initial run, our platform will gather information about the tests. During the second run, it will optimize it to fit within 15 minutes.
<Tabs>
<TabItem value="iOS" label="iOS">

```shell
# Download the prebuilt iOS Application
curl https://cloud.marathonlabs.io/samples/ios/sample.zip -o sample.zip

# Download the prebuilt iOS Test Application
curl https://cloud.marathonlabs.io/samples/ios/sampleUITests-Runner.zip -o sampleUITests-Runner.zip
```

</TabItem>	
<TabItem value="Android" label="Android">

```shell
# Download the prebuilt Android Application
curl https://cloud.marathonlabs.io/samples/android/app.apk -o app.apk

# Download the prebuilt Android Test Application
curl https://cloud.marathonlabs.io/samples/android/appTest.apk -o appTest.apk
```

</TabItem>
</Tabs>

### Execution

Now you can start running your tests. Use the following command to execute the CLI with the necessary parameters:
```shell
marathon-cloud -api_key api_key -apk app.apk -testapk appTest.apk
```
For additional parameters, refer to the [marathon-cloud-cli README](https://github.com/MarathonLabs/marathon-cloud-cli/#installation).

## How to go from A to Z

If you are looking for detailed documentation of Marathon Cloud then please read the correspondent article [for iOS](https://medium.marathonlabs.io/run-ios-tests-with-marathon-cloud-692a75213c86) and [for Android](https://medium.marathonlabs.io/run-android-tests-with-marathon-cloud-98de3bfb0ff0).
