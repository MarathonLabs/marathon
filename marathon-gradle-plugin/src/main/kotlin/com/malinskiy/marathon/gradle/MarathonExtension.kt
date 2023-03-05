package com.malinskiy.marathon.gradle

import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.strategy.ExecutionStrategyConfiguration
import com.malinskiy.marathon.config.vendor.android.AdbEndpoint
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.config.vendor.android.TestAccessConfiguration
import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration
import com.malinskiy.marathon.config.vendor.android.TimeoutConfiguration
import com.malinskiy.marathon.gradle.configuration.PoolingStrategyConfiguration
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.internal.ConfigureUtil
import java.io.File

open class MarathonExtension {
    /**
     * This string specifies the name of this test run configuration. It is used mainly in the generated test reports.
     */
    var name: String = "Marathon"

    var analyticsConfiguration: AnalyticsConfig? = null

    /**
     * Pooling strategy affects how devices are grouped together
     */
    var poolingStrategy: PoolingStrategyConfiguration? = null

    /**
     * Sharding is a mechanism that allows the marathon to affect the tests scheduled for execution inside each pool
     */
    var shardingStrategy: ShardingStrategyConfiguration? = null

    /**
     * In order to optimise the performance of test execution tests need to be sorted. This requires analytics backend enabled since we need
     * historical data in order to anticipate tests behaviour like duration and success/failure rate
     */
    var sortingStrategy: SortingStrategyConfiguration? = null

    /**
     * Batching mechanism allows you to trade off stability for performance. A group of tests executed using one single run is called a batch. Most
     * of the time, this means that between tests in the same batch you're sharing the device state so there is no clean-up. On the other hand you
     * gain some performance improvements since the execution command usually is quite slow (up to 10 seconds for some platforms).
     */
    var batchingStrategy: BatchingStrategyConfiguration? = null

    /**
     * This is the main anticipation logic for marathon. Using the analytics backend we can understand the success rate and hence queue preventive
     * retries to mitigate the flakiness of the tests and environment
     */
    var flakinessStrategy: FlakinessStrategyConfiguration? = null

    /**
     * This is the logic that kicks in if preventive logic failed to anticipate such high number of retries. This works after the tests were
     * actually executed
     */
    var retryStrategy: RetryStrategyConfiguration? = null

    /**
     * In order to indicate to marathon which tests you want to execute you can use the allowlist and blocklist parameters.
     *
     * First allowlist is applied, then the blocklist. Each accepts a *TestFilter*:
     *
     * | YAML type                         | Gradle class                                    | Description                                                                                |
     * | --------------------------------- |:-----------------------------------------------:| ------------------------------------------------------------------------------------------:|
     * | "fully-qualified-test-name"       | `FullyQualifiedTestnameFilterConfiguration`     | Filters tests by their FQTN which is `$package.$class#$method`. The `#` sign is important! |
     * | "fully-qualified-class-name"      | `FullyQualifiedClassnameFilterConfiguration`    | Filters tests by their FQCN which is `$package.$class`                                     |
     * | "simple-class-name"               | `SimpleClassnameFilterConfiguration`            | Filters tests by using only test class name, e.g. `MyTest`                                 |
     * | "package"                         | `TestPackageFilterConfiguration`                | Filters tests by using only test package, e.g. `com.example`                               |
     * | "method"                          | `TestMethodFilterConfiguration`                 | Filters tests by using only test method, e.g. `myComplicatedTest`                          |
     * | "annotation"                      | `AnnotationFilterConfiguration`                 | Filters tests by using only test annotation name, e.g. `androidx.test.filters.LargeTest`   |
     *
     * All the filters can be used in allowlist and in blocklist block as well, for example the following will run only smoke tests:
     *
     * ```yaml
     * allowlist:
     * - type: "annotation"
     * values:
     * - "com.example.SmokeTest"
     * ```
     *
     * And the next snippet will execute everything, but the smoke tests:
     *
     * ```yaml
     * blocklist:
     * - type: "annotation"
     * values:
     * - "com.example.SmokeTest"
     * ```
     *
     * ### Filter parameters
     *
     * Each of the above filters expects **only one** of the following parameters:
     *
     * - A `regex` for matching
     * - An array of `values`
     * - A `file` that contains each value on a separate line (empty lines will be ignored)
     *
     * ### Regex filtering
     *
     * An example of `regex` filtering is executing any test for a particular package, e.g. for package: `com.example` and it's subpackages:
     *
     * ```yaml
     * allowlist:
     * - type: "package"
     * regex: "com\.example.*"
     * ```
     *
     * ### Values filtering
     *
     * You could also specify each package separately via values:
     *
     * ```yaml
     * allowlist:
     * - type: "package"
     * values:
     * - "com.example"
     * - "com.example.subpackage"
     * ```
     *
     * ### Values file filtering
     *
     * Or you can supply these packages via a file (be careful with the relative paths: they will be relative to the workdir of the process):
     *
     * ```yaml
     * allowlist:
     * - type: "package"
     * file: "testing/myfilterfile"
     * ```
     *
     * Inside the `testing/myfilterfile` you should supply the values, each on a separate line:
     *
     * ```
     * com.example
     * com.example.subpackage
     * ```
     */
    var filteringConfiguration: FilteringPluginConfiguration? = null

    /**
     *
     * Directory path to use as the root folder for all the runner output (logs, reports, etc).
     *
     * For gradle, the output path will automatically be set to a `marathon` folder in your reports folder unless it's overridden.
     *
     */
    var baseOutputDir: String? = null


    var outputConfiguration: OutputConfiguration? = null
    fun outputConfiguration(action: Action<OutputConfiguration>) {
        outputConfiguration = OutputConfiguration().also { action.execute(it) }
    }

    fun outputConfiguration(closure: Closure<OutputConfiguration>) = outputConfiguration(ConfigureUtil.configureUsing(closure))

    /**
     * By default, the build fails if some tests failed. If you want to the build to succeed even if some tests failed use *true*.
     */
    var ignoreFailures: Boolean? = null

    /**
     * Depending on the vendor implementation code coverage may not be supported. By default, code coverage is disabled. If this option is enabled,
     * code coverage will be collected and marathon assumes that code coverage generation will be setup by user (e.g. proper build flags, jacoco
     * jar added to classpath, etc).
     */
    var isCodeCoverageEnabled: Boolean? = null

    /**
     * When executing tests with retries there are multiple trade-offs to be made. Two execution strategies are supported: any success or all success.
     * By default, any success strategy is used with fast execution i.e. if one of the test retries succeeds then the test is considered successfully
     * executed and all non-started retries are removed.
     */
    var executionStrategy: ExecutionStrategyConfiguration? = null

    /**
     * By default, tests that don't have any status reported after execution (for example a device disconnected during the execution) retry
     * indefinitely. You can limit the number of total execution for such cases using this option.
     */
    var uncompletedTestRetryQuota: Int? = null

    /**
     * By default, test classes are found using the ```"^((?!Abstract).)*Test[s]*$"``` regex. You can override this if you need to.
     */
    var testClassRegexes: Collection<String>? = null


    var includeSerialRegexes: Collection<String>? = null
    var excludeSerialRegexes: Collection<String>? = null

    /**
     * This parameter specifies the behaviour for the underlying test executor to timeout if the batch execution exceeded some duration. By
     * default, this is set to 30 minutes.
     */
    var testBatchTimeoutMillis: Long? = null

    /**
     * This parameter specifies the behaviour for the underlying test executor to timeout if there is no output. By default, this is set to 5
     * minutes.
     */
    var testOutputTimeoutMillis: Long? = null

    /**
     * Enabled very verbose logging to stdout of all the marathon components. Very useful for debugging.
     */
    var debug: Boolean? = null

    /**
     * By default, screen recording will only be pulled for tests that failed (**ON_FAILURE** option). This is to save space and also to reduce the
     * test duration time since we're not pulling additional files. If you need to save screen recording regardless of the test pass/failure please
     * use the **ON_ANY** option
     */
    var screenRecordingPolicy: ScreenRecordingPolicy? = null

    /**
     * To better understand the use-cases that marathon is used for we're asking you to provide us with anonymised information about your usage. By
     * default, this is disabled. Use **true** to enable.
     */
    var analyticsTracking: Boolean = false

    /**
     * When the test run starts device provider is expected to provide some devices. This should not take more than 3 minutes by default. If your
     * setup requires this to be changed please override using this parameter
     */
    var deviceInitializationTimeoutMillis: Long? = null

    /**
     * By default, marathon does not clear state between test batch executions. To mitigate potential test side-effects, one could add an option to clear the package data between test runs. Keep in mind that test side-effects might be present.
     * If you want to isolate tests even further, then you should consider reducing the batch size.
     *
     * Since `pm clear` resets the permissions of the package, the granting of permissions during installation is essentially overridden. Marathon doesn't grant the permissions again.
     * If you need permissions to be granted and you need to clear the state, consider alternatives like [GrantPermissionRule](https://developer.android.com/reference/androidx/test/rule/GrantPermissionRule)
     *
     */
    var applicationPmClear: Boolean? = null

    /**
     * By default, marathon does not clear state between test batch executions. To mitigate potential test side-effects, one could add an option to clear the package data between test runs. Keep in mind that test side-effects might be present.
     * If you want to isolate tests even further, then you should consider reducing the batch size.
     *
     * Since `pm clear` resets the permissions of the package, the granting of permissions during installation is essentially overridden. Marathon doesn't grant the permissions again.
     * If you need permissions to be granted and you need to clear the state, consider alternatives like [GrantPermissionRule](https://developer.android.com/reference/androidx/test/rule/GrantPermissionRule)
     *
     */
    var testApplicationPmClear: Boolean? = null

    /**
     * This option will allow you to increase/decrease the default adb init timeout of 30 seconds.
     */
    var adbInitTimeout: Int? = null

    /**
     * By default, these will be ```-g -r``` (```-r``` prior to marshmallow). You can specify additional options to append to the default ones.
     */
    var installOptions: String? = null

    /**
     * This option allows to customise how marathon assigns a serial number to devices.
     *
     * Notes on the source of serial number:
     *
     * * ```marathon_property``` - Property name `marathon.serialno`
     * * ```boot_property``` - Property name `ro.boot.serialno`
     * * ```hostname``` - Property name `net.hostname`
     * * ```ddms``` - Adb serial number(same as you see with `adb devices` command)
     * * ```automatic``` - Sequentially checks all available options for first non-empty value.
     *
     * Priority order:
     * Before 0.6: ```marathon_property``` -> ```boot_property``` -> ```hostname``` -> ```ddms``` -> UUID
     * After 0.6:  ```marathon_property``` -> ```ddms``` -> ```boot_property``` -> ```hostname``` -> UUID
     */
    var serialStrategy: SerialStrategy? = null

    /**
     * By default, device will record a 1280x720 1Mbps video of up to 180 seconds if it is supported. If on the other hand you want to force
     * screenshots or configure the recording parameters you can override using this parameter
     */
    var screenRecordConfiguration: ScreenRecordConfiguration? = null

    var waitForDevicesTimeoutMillis: Long? = null

    /**
     * If you want to enable on-device collection of allure's reports, you can use this option
     */
    var allureConfiguration: AllureConfiguration? = null

    /**
     * With the introduction of [adam](https://github.com/Malinskiy/adam) we can precisely control the timeout of individual requests
     */
    var timeoutConfiguration: TimeoutConfiguration? = null

    /**
     * Sometimes you need to pull some folders from each device after the test execution. It may be screenshots or logs or other debug information.
     * To help with this marathon supports pulling files from devices at the end of the test batch execution.
     */
    var fileSyncConfiguration: FileSyncConfiguration? = null

    /**
     * Test parsing (collecting a list of tests expected to execute) can be done using either a local test parser, which uses byte code analysis,
     * or a remote test parser that uses an Android device to collect a list of tests expected to run. Both have pros and cons listed below:
     *
     * | YAML type     | Gradle class          | Pros                                                                                                                | Const                                                                                                                                                                             |
     * | --------------|:---------------------:| ------------------------------------------------------------------------------------------------------------------ :| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- :|
     * | "local"       | `LocalTestParser`     | Doesn't require a booted Android device                                                                             | Doesn't support runtime-generated tests, e.g. named parameterized tests. Doesn't support parallelising parameterized tests                                                        |
     * | "remote"      | `RemoteTestParser`    | Supports any runtime-generated tests, including parameterized, and allows marathon to parallelise their execution   | Requires a booted Android device for parsing. If you need to use annotations for filtering purposes, requires test apk changes as well as attaching a test run listener for parser|
     *
     * Default test parser is local. If you need to parallelize the execution of parameterized tests or have complex runtime test generation
     * (custom test runners, e.g. cucumber) - remote parser is your choice.
     *
     * For annotations parsing using remote test parser test run is triggered without running tests (using `-e log true` option). Annotations are
     * expected to be reported as test metrics, e.g.:
     *
     * ```text
     * INSTRUMENTATION_STATUS_CODE: 0
     * INSTRUMENTATION_STATUS: class=com.example.FailedAssumptionTest
     * INSTRUMENTATION_STATUS: current=4
     * INSTRUMENTATION_STATUS: id=AndroidJUnitRunner
     * INSTRUMENTATION_STATUS: numtests=39
     * INSTRUMENTATION_STATUS: stream=
     * com.example.FailedAssumptionTest:
     * INSTRUMENTATION_STATUS: test=ignoreTest
     * INSTRUMENTATION_STATUS_CODE: 1
     * INSTRUMENTATION_STATUS: com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer.v2=[androidx.test.filters.SmallTest(), io.qameta.allure.kotlin.Severity(value=critical), io.qameta.allure.kotlin.Story(value=Slow), org.junit.Test(expected=class org.junit.Test$None:timeout=0), io.qameta.allure.kotlin.Owner(value=user2), io.qameta.allure.kotlin.Feature(value=Text on main screen), io.qameta.allure.kotlin.Epic(value=General), org.junit.runner.RunWith(value=class io.qameta.allure.android.runners.AllureAndroidJUnit4), kotlin.Metadata(bytecodeVersion=[I@bdf6b25:data1=[Ljava.lang.String;@46414fa:data2=[Ljava.lang.String;@5d4aab:extraInt=0:extraString=:kind=1:metadataVersion=[I@fbb1508:packageName=), io.qameta.allure.kotlin.Severity(value=critical), io.qameta.allure.kotlin.Story(value=Slow)]
     * INSTRUMENTATION_STATUS_CODE: 2
     * INSTRUMENTATION_STATUS: class=com.example.FailedAssumptionTest
     * INSTRUMENTATION_STATUS: current=4
     * INSTRUMENTATION_STATUS: id=AndroidJUnitRunner
     * INSTRUMENTATION_STATUS: numtests=39
     * INSTRUMENTATION_STATUS: stream=.
     * INSTRUMENTATION_STATUS: test=ignoreTest
     * ```
     *
     * To generate the above metrics you need to add a JUnit 4 listener to your dependencies:
     *
     * ```kotlin
     * dependecies {
     *   androidTestImplementation("com.malinskiy.adam:android-junit4-test-annotation-producer:${LATEST_VERSION}")
     * }
     *  ```
     *
     * Then you need to attach it to the execution. One way to attach the listener is using `am instrument` parameters, e.g.
     * `-e listener com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer`.
     */
    var testParserConfiguration: TestParserConfiguration? = null

    /**
     * This option will grant all runtime permissions during the installation of the
     * application. This works like the option `-g` for ```adb install``` command. By default, it's set to **false**.
     */
    var autoGrantPermission: Boolean? = null

    /**
     * If you want to pass additional arguments to the `am instrument` command executed on the device
     */
    var instrumentationArgs: MutableMap<String, String> = mutableMapOf()

    /**
     * Marathon supports adam's junit extensions which allow tests to gain access to adb on all devices and emulator's control + gRPC port. See the
     * [docs](https://malinskiy.github.io/adam/extensions/1-android-junit/) as well as the [PR](https://github.com/Malinskiy/adam/pull/30) for
     * description on how this works.
     *
     */
    var testAccessConfiguration: TestAccessConfiguration? = null

    /**
     * Default configuration of marathon assumes that adb server is started locally and is available at `127.0.0.1:5037`. In some cases it may be
     * desirable to connect multiple adb servers instead of connecting devices to a single adb server. An example of this is distributed execution
     * of tests using test access (calling adb commands from tests). For such scenario all emulators should be connected via a local (in relation
     * to the emulator) adb server. Default port for each host is 5037.
     *
     * In order to expose the adb server it should be started on all or public network interfaces using option `-a`. For example, if you want to
     * expose the adb server and start it in foreground explicitly on port 5037: `adb nodaemon server -a -P 5037`.
     *
     */
    var adbServers: List<AdbEndpoint>? = null

    /**
     * Install extra apk before running the tests if required, e.g. test-butler.apk
     */
    var extraApplications: List<File>? = null

    /**
     * By default, instrumentation uses --no-window-animation flag. Use this option if you want to enable window animations
     */
    var disableWindowAnimation: Boolean? = null

    /**
     * Configuration of analytics backend to be used for storing and retrieving test metrics. This plays a major part in optimising
     * performance and mitigating flakiness.
     */
    fun analytics(action: Action<AnalyticsConfig>) {
        analyticsConfiguration = AnalyticsConfig().also { action.execute(it) }
    }

    /**
     * Batching mechanism allows you to trade off stability for performance. A group of tests executed using one single run is called a batch. Most
     * of the time, this means that between tests in the same batch you're sharing the device state so there is no clean-up. On the other hand you
     * gain some performance improvements since the execution command usually is quite slow (up to 10 seconds for some platforms).
     */
    fun batchingStrategy(action: Action<BatchingStrategyConfiguration>) {
        batchingStrategy = BatchingStrategyConfiguration().also { action.execute(it) }
    }

    /**
     * This is the main anticipation logic for marathon. Using the analytics backend we can understand the success rate and hence queue preventive
     * retries to mitigate the flakiness of the tests and environment
     */
    fun flakinessStrategy(action: Action<FlakinessStrategyConfiguration>) {
        flakinessStrategy = FlakinessStrategyConfiguration().also { action.execute(it) }
    }

    /**
     * Pooling strategy affects how devices are grouped together
     */
    fun poolingStrategy(action: Action<PoolingStrategyConfiguration>) {
        poolingStrategy = PoolingStrategyConfiguration().also { action.execute(it) }
    }

    /**
     * This is the logic that kicks in if preventive logic failed to anticipate such high number of retries. This works after the tests were
     * actually executed
     */
    fun retryStrategy(action: Action<RetryStrategyConfiguration>) {
        retryStrategy = RetryStrategyConfiguration().also { action.execute(it) }
    }

    /**
     * Sharding is a mechanism that allows the marathon to affect the tests scheduled for execution inside each pool
     */
    fun shardingStrategy(action: Action<ShardingStrategyConfiguration>) {
        shardingStrategy = ShardingStrategyConfiguration().also { action.execute(it) }
    }

    /**
     * In order to optimise the performance of test execution tests need to be sorted. This requires analytics backend enabled since we need
     * historical data in order to anticipate tests behaviour like duration and success/failure rate
     */
    fun sortingStrategy(action: Action<SortingStrategyConfiguration>) {
        sortingStrategy = SortingStrategyConfiguration().also { action.execute(it) }
    }

    /**
     * In order to indicate to marathon which tests you want to execute you can use the allowlist and blocklist parameters.
     *
     * First allowlist is applied, then the blocklist. Each accepts a *TestFilter*:
     *
     * | YAML type                         | Gradle class                                    | Description                                                                                |
     * | --------------------------------- |:-----------------------------------------------:| ------------------------------------------------------------------------------------------:|
     * | "fully-qualified-test-name"       | `FullyQualifiedTestnameFilterConfiguration`     | Filters tests by their FQTN which is `$package.$class#$method`. The `#` sign is important! |
     * | "fully-qualified-class-name"      | `FullyQualifiedClassnameFilterConfiguration`    | Filters tests by their FQCN which is `$package.$class`                                     |
     * | "simple-class-name"               | `SimpleClassnameFilterConfiguration`            | Filters tests by using only test class name, e.g. `MyTest`                                 |
     * | "package"                         | `TestPackageFilterConfiguration`                | Filters tests by using only test package, e.g. `com.example`                               |
     * | "method"                          | `TestMethodFilterConfiguration`                 | Filters tests by using only test method, e.g. `myComplicatedTest`                          |
     * | "annotation"                      | `AnnotationFilterConfiguration`                 | Filters tests by using only test annotation name, e.g. `androidx.test.filters.LargeTest`   |
     *
     * All the filters can be used in allowlist and in blocklist block as well, for example the following will run only smoke tests:
     *
     * ```yaml
     * allowlist:
     * - type: "annotation"
     * values:
     * - "com.example.SmokeTest"
     * ```
     *
     * And the next snippet will execute everything, but the smoke tests:
     *
     * ```yaml
     * blocklist:
     * - type: "annotation"
     * values:
     * - "com.example.SmokeTest"
     * ```
     *
     * ### Filter parameters
     *
     * Each of the above filters expects **only one** of the following parameters:
     *
     * - A `regex` for matching
     * - An array of `values`
     * - A `file` that contains each value on a separate line (empty lines will be ignored)
     *
     * ### Regex filtering
     *
     * An example of `regex` filtering is executing any test for a particular package, e.g. for package: `com.example` and it's subpackages:
     *
     * ```yaml
     * allowlist:
     * - type: "package"
     * regex: "com\.example.*"
     * ```
     *
     * ### Values filtering
     *
     * You could also specify each package separately via values:
     *
     * ```yaml
     * allowlist:
     * - type: "package"
     * values:
     * - "com.example"
     * - "com.example.subpackage"
     * ```
     *
     * ### Values file filtering
     *
     * Or you can supply these packages via a file (be careful with the relative paths: they will be relative to the workdir of the process):
     *
     * ```yaml
     * allowlist:
     * - type: "package"
     * file: "testing/myfilterfile"
     * ```
     *
     * Inside the `testing/myfilterfile` you should supply the values, each on a separate line:
     *
     * ```
     * com.example
     * com.example.subpackage
     * ```
     */
    fun filteringConfiguration(action: Action<FilteringPluginConfiguration>) {
        filteringConfiguration = FilteringPluginConfiguration().also { action.execute(it) }
    }

    /**
     * If you want to pass additional arguments to the `am instrument` command executed on the device
     */
    fun instrumentationArgs(action: Action<MutableMap<String, String>>) {
        instrumentationArgs = mutableMapOf<String, String>().also { action.execute(it) }
    }

    /**
     * If you want to enable on-device collection of allure's reports, you can use this option
     */
    fun allureConfiguration(action: Action<AllureConfiguration>) {
        allureConfiguration = AllureConfiguration().also { action.execute(it) }
    }

    /**
     * With the introduction of [adam](https://github.com/Malinskiy/adam) we can precisely control the timeout of individual requests
     */
    fun timeoutConfiguration(action: Action<TimeoutConfiguration>) {
        timeoutConfiguration = TimeoutConfiguration().also { action.execute(it) }
    }

    /**
     * Sometimes you need to pull some folders from each device after the test execution. It may be screenshots or logs or other debug information.
     * To help with this marathon supports pulling files from devices at the end of the test batch execution.
     *
     * Please pay attention to the path on the device: it's a relative path to the `Environment.getExternalStorageDirectory()` or
     * the `EXTERNAL_STORAGE` envvar. In practice this means that if you have a folder like `/sdcard/my-folder` you should specify `/my-folder` as
     * a relative path.
     *
     * Starting with Android 11 your test application will require MANAGE_EXTERNAL_STORAGE permission:
     *
     * ```xml
     * <?xml version="1.0" encoding="utf-8"?>
     * <manifest xmlns:android="http://schemas.android.com/apk/res/android">
     * <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>
     *  ...
     * </manifest>
     *  ```
     *
     *  Marathon will automatically grant this permission before executing tests if you pull any files from devices.
     */
    fun fileSyncConfiguration(action: Action<FileSyncConfiguration>) {
        fileSyncConfiguration = FileSyncConfiguration().also { action.execute(it) }
    }
}
