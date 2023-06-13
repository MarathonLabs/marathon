---
title: "Overview"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Marathon Cloud is a cloud testing infrastructure built on top of the Marathon Runner.
It automatically provisions virtual devices to accommodate your tests within 15 minutes.
The test execution is then delegated to Marathon Runner, which handles tasks such as batching, 
sorting, preventive retries, and post-factum retries. 
This ensures an even distribution of tests across the provisioned devices. 


## What you'll need

To utilize Marathon Cloud, please ensure you have the following:
- Marathon Cloud account (Sign up or Sign here)
- API Key: You can create an API Key by following the link provided.
- CLI or CI/CD plugin: Make sure you have the Command Line Interface (CLI) or the appropriate Continuous Integration/Continuous Deployment (CI/CD) plugin installed.
- Application with tests for iOS or Android

## Application requirements

### iOS

Marathon Cloud supports tests written with XCTest and XCUITest frameworks.
Both the application and the tests must be built for the ARM architecture.
When dealing with iOS applications and tests, please compress them into ZIP archives.
For instance, if your project is named "SampleApp," navigate to Product -> Show Build Folder in Finder. 
In the opened Finder window, you'll find the required folders/applications for testing: "SampleApp.app" for the application and "SampleAPPUITests-Runner.app" for the Testing Application.

### Android

Marathon Cloud supports tests written with UIAutomator, Espresso, and Kaspresso frameworks.
You will need APK files for both the application and the tests.

## Installation

Please look at CLI [Readme on Github](https://github.com/MarathonLabs/marathon-cloud-cli).
