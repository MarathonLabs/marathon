import CloudSign from '/img/cloud_sign.png';
import CloudDialog from '/img/cloud_android_rundialog.png';

# Android Run

Quick introduction to run your first Android UI tests at [Marathon Cloud](marathonlabs.io).

## Sign up / Sign in

Go to [the main page](marathonlabs.io) and click on "Sign up / Sign in" button:
![cloud main](/img/cloud_main.png)

Pass the Login/Registration flow:
<!-- ![cloud sign](/img/cloud_sign.png) -->
<img src={CloudSign} width="400"/>

## Dashboard

After Sign up / Sign in you'll see the dashboard:
![cloud dashboard](/img/cloud_dashboard_temp.png)


## Build app and test APKs

Before initiating the testing process for your application, you'll require two APK files: one for the application that's being tested, and another for the tests themselves. Typically, `debug` variants are utilized for this purpose.

If the primary application resides under the `app/` subproject, you can execute the following command to build both the app and test APK:

```bash
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
```

Be sure to note the relative paths of the test APK and the app APK, as they will be required for running the tests. In the context of our example, involving the `app` project and the `debug` build, these files can be located at the following paths:

- App APK: `app/build/outputs/apk/debug/app-debug.apk`
- Test APK: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`

## Run tests with UI

The simplest way to run your tests is to use UI Dashboard. First of all, click on the "New Run" button located in the top right corner:
![cloud android new run](/img/cloud_android_newrun.png)

The following dialog will appear: 
<!-- ![cloud android new run dialog](/img/cloud_android_rundialog.png) -->
<img src={CloudDialog} width="400"/>

Your next actions in this dialog should be as follows:

- Select the "Android" tab.
- Enter a title of your choice.
- Choose the application apk. In accordance with the previous example, the apk can be found at this path: `app/build/outputs/apk/debug/app-debug.apk`.
- Select the test apk. As per the same example, the test apk is located at this path: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.
- Click on the "Run" button.

Congratulations! You've started your first test run at Marathon Cloud:
![cloud android new run dialog](/img/cloud_android_progress.png)

## Watching results

Your run should be completed within a timeframe of 15 minutes. Once finished, you can view the reports by clicking on the designated button:
![cloud android click report](/img/cloud_android_click_report.png)

The overview of your test run will appear:
![cloud android report overview](/img/cloud_android_report_overview.png)

If you wish to examine the reasons for any test failures, please follow these next steps:
![cloud android report error 1](/img/cloud_android_report_error_1.png)

![cloud android report error 2](/img/cloud_android_report_error_2.png)

![cloud android report error 3](/img/cloud_android_report_error_3.png)

