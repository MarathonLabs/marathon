---
title: "Overview"
---

### 
[Marathon Cloud](https://cloud.marathonlabs.io) is a cloud testing infrastructure built on top of the Marathon test runner. 
It automatically provisions virtual devices to accommodate your tests within 15 minutes. 
The test execution is then delegated to Marathon test runner, which handles tasks such as batching, sorting, preventive retries, and post-factum retries. 
This ensures an even distribution of tests across the provisioned devices.

### How it works
Whenever you submit an application and test application to Marathon Cloud, the following steps are taken:
- Calculation of the necessary number of devices
- Provisioning of the devices
- Distribution and execution of the tests
- Control of the tests and real-time load balancing
- Generation of reports
- Uploading of artifacts and the generated report


### Device provisioning

When you run tests with Marathon Cloud for the first time, we begin storing the test history in our database. 
The next time you run these tests, we already have information on the average time and the probability of a successful execution for each test. 
Using this data, we calculate the necessary number of devices to ensure that your tests will complete within 15 minutes, whether that requires 5 devices or 200. 
We also monitor the progress of test executions and adjust the distribution of tests across devices as needed. 
While the tests are running, our service can dynamically increase the number of devices to expedite the execution process.

### Batching

Balancing speed and stability is one of the primary challenges for Marathon Cloud. 
In order to maintain fast test execution, we employ a batching strategy where we group 5 tests together in a single batch. 
This approach involves executing 5 tests consecutively, and afterward, we reset the device to a clean state. 
If you prefer, you can enable the "isolated" parameter to manage device cleaning yourself, 
but please note that this may lead to an increase in the number of devices required and the overall time of devices taken for testing. 
However, the total execution time will still be 15 minutes.

### Retries
Retries are a cornerstone of effective testing. The larger your test suite, the greater the likelihood that one test might fail due to environmental instability, commonly known as test flakiness. While adding retries can improve test stability, it also increases the total testing time.
Marathon offers two retry strategies and several enhancements to strike a balance between stability and efficiency:
- Predictive retries: By analyzing past test executions, we can determine the success rate of each test. For flaky tests, we can run multiple executions to ensure at least one succeeds.
- Common retries: If a test fails, we will rerun it up to three times. Marathon will attempt to run the test on a different device to minimize environmental impact on the results.
- Additional features: Sorting, batching, test rebalancing, and more.









