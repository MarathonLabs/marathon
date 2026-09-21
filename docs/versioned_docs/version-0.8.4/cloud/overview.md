---
title: "Overview"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

[Marathon Cloud](https://cloud.marathonlabs.io/) is a cloud testing infrastructure built on top of the Marathon test runner.
It automatically provisions virtual devices to accommodate your tests within 15 minutes.
The test execution is then delegated to Marathon test runner, which handles tasks such as batching, 
sorting, preventive retries, and post-factum retries. 
This ensures an even distribution of tests across the provisioned devices. 

## Install

The installation can be performed using [Homebrew](https://brew.sh/):
```shell
brew install malinskiy/tap/marathon-cloud
```
Alternatively, you can download prebuilt binaries for Windows, Linux, or MacOS from [the Release page](https://github.com/MarathonLabs/marathon-cloud-cli/releases).

## API Key

Token creation and management are available at [the Tokens page](https://cloud.marathonlabs.io/tokens). Generate a token and save it somewhere safe for the next step.


