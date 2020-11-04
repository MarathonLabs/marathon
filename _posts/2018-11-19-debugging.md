---
layout: docs
title: "Debugging"
category: doc
date: 2018-11-19 16:55:00
order: 6
---

### Logs & Reports
There are multiple logs and reports available in marathon in order to understand what's going on. Let's check all of them.

#### Stdout log
The first and easiest one to get is the stdout log that is printed during the execution of the test run. In order to have more information you can enable the debug mode via configuration.

#### Raw json log
In case you want to produce a custom report or you want to push metrics based on the results of the execution raw json is probably your best option. Each test is written as a json object inside an array. Keep in mind that this report shows retries, so you basically have full access to what happened during the execution.

```json
{
  "package": "com.example",
  "class": "SomeTest",
  "method": "checkSomethingActuallyWorks",
  "deviceSerial": "XXXXXXXXXXXX",
  "ignored": false,
  "success": true,
  "timestamp": 1548142665055,
  "duration": 13370
}
```

#### Html report
This report shows test result (success, failure, ignored), has the video/screencapture if it's available and also device's log. Filtering by test name and class is supported. Device pools are separated on the main page of report.

#### JUnit4 report
Xml files are written according to the Junit4 specification to integrate with existing tools such as Continuous Integration servers or third-party tools such as Danger.

#### Timeline log
This report helps identify potential misbehaviours visually helping infrastructure teams identify faulty devices and also helping developers identify tests which affect the whole batch. For example you have a test which doesn't cleanup properly after execution and all the tests after this one will fail in the same batch.

#### Allure report
[allure][1] reports helps to identify multiple possible problems during test execution. Be aware that marathon generates only the data files necessary to compile the actual report via commands line or plugin options available for allure.

One of the best use cases for this report is the grouping of problems which helps to identify if your tests have a specific issue that is relevant for a large number of tests. Another useful feature is the ability to see all the retries visually and with video/screencapture (if available). Allure also provides the ability to see flaky tests by storing history of the runs.

### Dashboards
Marathon is able to leverage the power of InfluxDB and Graphite to store and fetch the metrics about the test runs. There are sample dashboards for [InfluxDB][3] and [Graphite][4] that you can use in [grafana][2] to [visualise][5] this information in order to better understand what's going on.

### Common use cases
#### Investigating specific device problems
Sometimes the execution device is incorrectly configured or doesn't work as expected. You have several options to identify such problems:
* grafana dashboard and see the distribution of test failures by device
* Timeline report
* Allure's timeline report

#### Checking overall test flakiness
It's quite easy to check the overall status of your tests if you navigate to the grafana dashboard based on the InfluxDB data and check test pass probability distribution. Ideally you should have a lot of tests either at the 1 bucket and 0. Everything in-between affects the test performance drastically especially the buckets closer to the 1 like *[0.9 - 1.0)* so it's much more desirable to have tests which have low probability of passing instead of the tests which have 0.9999 probability of passing (*sometimes* fails).

#### Investigating if there is some common problem with your tests
Assuming that your common problem throws the same message in the exception in different tests you can quickly understand this if you navigate to **Categories** tab in the Allure report and check the number of occurrences on the right for each problem.

#### Checking quality by specific feature/team/epic
Marathon supports tests that have meta-information such as Epic, Team or Feature. This makes it possible to understand for example if this test corresponds to a specific feature. Allure report visualises this information and makes it possible to navigate all of them using the **Behaviors** page.

#### Checking the load distribution of test run
Sometimes your devices might not be utilised during all of the execution time. To check this you need to have a timeline. This is available either in timeline report or allure. Keep in mind that allure report doesn't contain the retries whereas the marathon's timeline report does.

#### Checking the retry count of a test
Retries can be checked either by allure report. Test pass probability on the other hand is basically the reason why we do retries so grafana dashboard is another option how you can check the expected and observed retries.

[1]: https://github.com/allure-framework/allure2/
[2]: https://grafana.com/
[3]: https://github.com/Malinskiy/marathon/blob/develop/assets/influxdb-grafana-dashboard.json
[4]: https://github.com/Malinskiy/marathon/blob/develop/assets/graphite-grafana-dashboard.json
[5]: https://snapshot.raintank.io/dashboard/snapshot/j5rbxzFhfMDG6eKIcB9sLcH16IICyzvW?orgId=2
