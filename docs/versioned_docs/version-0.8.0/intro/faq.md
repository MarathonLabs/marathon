---
title: "FAQ"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Below is a list of frequently asked questions

## How to investigate device-specific problems?
Sometimes the execution device is incorrectly configured or doesn't work as expected. You have several options to identify such problems:
* grafana dashboard and see the distribution of test failures by device
* Timeline report
* Allure's timeline report

## How to check overall test flakiness?
It's quite easy to check the overall status of your tests if you navigate to the grafana dashboard based on the InfluxDB data and check
 test pass probability distribution. Ideally you should have a lot of tests either at the 1 bucket and 0. Everything in-between affects the
 test performance drastically especially the buckets closer to the 1 like *[0.9 - 1.0)* so it's much more desirable to have tests which have
 low probability of passing instead of the tests which have 0.9999 probability of passing (*sometimes* fails).

## How to check for common underlying problems in tests?
Assuming your common problem throws the same message in the exception in different tests you can quickly understand this if you
 navigate to **Categories** tab in the Allure report and check the number of occurrences on the right for each problem.

## How to check quality by specific feature/team/epic?
Marathon supports tests that have meta-information such as Epic, Team or Feature from allure. This makes it possible to understand, for
 example, if a particular test corresponds to a specific feature. Allure report visualises this information and makes it possible to
 navigate all of them using the **Behaviors** page.

## How to check the load distribution of test run?
Sometimes your devices might not be utilised during all of the execution time. To check this you need to have a timeline. This is available
 either in timeline report or allure. Keep in mind that allure report doesn't contain the retries whereas the marathon's timeline report
 does.

## How to check the retry count of a test?
Retries can be checked in the allure report. Test pass probability on the other hand is the reason why we do retries so grafana dashboard is
 another option you can check for the expected and observed retries.

## My logs/screenshots are missing from the html report
If you're opening your reports with `file://` schema then it's a known issue: modern browsers reject our requests to additional files such 
 as logs or videos for security reasons. CI systems usually have webserver to host these files, but local environment can also start a 
 webserver for example using IntelliJ based IDEs by right clicking on the html file -> open in browser. Or doing something like 
 `python3 -m http.server 8000` in the directory of the report and navigating to `localhost`.

## Can I run my Android tests if my emulator(s) are on a remote instance?
Of course, you can do this by connecting your remote Android device (emulator or real device) by executing `adb connect $IP_OF_DEVICE`. 
 Assuming you have enabled adb over TCP/IP properly you should be able to execute your test on a remote Android device (or a hundred of 
 devices depending on how many you connect)
 
## How to execute a single test case?
This is possible and will depend on the distribution you're using.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
filteringConfiguration:
  allowlist:
  - type: "fully-qualified-class-name"
    regex: "com\.example\.MySingleTest"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
filteringConfiguration { 
    allowlist { 
        add(com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter("com\\.example\\.MySingleTest".toRegex()))
    }
}
```

</TabItem>
</Tabs>

## Some of my test artifacts (videos, screenshots, etc) are missing!
Due to the nature of marathon to device connection being unreliable, we can not guarantee that every single artifact is going to be there.
 Marathon tries best to pull necessary information from the device, but sometimes the device is unresponsive or just stopped working at all.
 If the test passed before it died marathon considers the test to be passing and resumes the execution.

## My test execution show 100%+ (e.g. 110%) progress. What's wrong?
This is an edge case with runtime-generated tests, e.g. Parameterized tests on Android, and how they're identified, executed and reported. 
It is possible to use remote test parsing using real devices on some platforms that support it, but in general there is no solution unless 
the platform supports proper test discovery.
