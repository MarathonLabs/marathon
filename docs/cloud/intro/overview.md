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
Using this data, we calculate the necessary number of devices to ensure that your tests will complete within 15 minutes. 
We also monitor the progress of test executions and adjust the distribution of tests across devices as needed. 
While the tests are running, our service can dynamically increase the number of devices to expedite the execution process.

### Batching

Balancing speed and stability is one of the primary challenges for Marathon Cloud. 
In order to maintain fast test execution, we employ a batching strategy where we group 5 tests together in a single batch. 
This approach involves executing 5 tests consecutively, and afterward, we reset the device to a clean state. 
If you prefer, you can enable the "isolated" parameter to manage device cleaning yourself, 
but please note that this may lead to an increase in the number of devices required and the overall time of devices taken for testing. 
However, the total execution time will still be 15 minutes.









