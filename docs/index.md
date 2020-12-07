---
layout: docs
title: "Cross-platform test runner for Android and iOS"
---

### About Marathon

Marathon is a fast and platform-independent test runner focused on performance and stability. Marathon offers easy to use platform implementations for Android and iOS as well as an API for use with custom hardware farms.

Marathon implements multiple key concepts related to test execution such as test batching, device pools, test sharding, test sorting, preventive retries as well as post-factum retries. By default most of these are set to conservative defaults but custom configurations are encouraged for those who want to optimize performance and/or stability.

Marathon's primary focus is on full control over the balance between stability of test execution and the overall test run performance.

### Configuration
Marathon is easy to configure either through CLI or using our grade plugin

### Performance
Marathon takes into account two key aspects of test execution:
* The duration of the test
* The probability of the test passing
In order for the test run to finish as quickly as possible one must balance the execution of tests with regards to the expected duration of the test. On the other hand we need to address the flakiness of the environment and the test itself. One key indicator of flakiness is the probability of the test passing.

Marathon takes a number of steps to ensure that each test run is as balanced as possible:
* The flakiness strategy queues up preventive retries for tests which are expected to fail during the test run according to the current real-time statistics
* The sorting strategy forces long tests to be executed first so that if an unexpected retry attempt occurs it doesn't affect the test run significantly (e.g. at the end of execution)
* If all else fail we revert back to post-factum retries but we try to limit it's impact on the run with retry quotas

### Getting Started
Start by visiting the [Download and Setup][1] page to learn how to integrate Marathon into your project. Then take a look at [Configuration][2] page to learn the basics of configuration. For more help and examples continue through the rest of the Documentation section, or take a look at one of our [sample apps][3].

### Requirements
Marathon requires Java 8 or higher.

[1]: {{ site.baseurl }}{% post_url 2018-11-19-downloading %}
[2]: {{ site.baseurl }}{% post_url 2018-11-19-configuration %}
[3]: {{ site.baseurl }}{% post_url 2018-11-19-samples %}
