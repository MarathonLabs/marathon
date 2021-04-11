package com.malinskiy.marathon.lite

import com.malinskiy.marathon.lite.configuration.AnalyticsConfiguration
import com.malinskiy.marathon.lite.configuration.DEFAULT_ANALYTICS_CONFIGURATION
import com.malinskiy.marathon.lite.configuration.DEFAULT_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.lite.configuration.DEFAULT_AUTO_GRANT_PERMISSION
import com.malinskiy.marathon.lite.configuration.DEFAULT_BATCHING_STRATEGY
import com.malinskiy.marathon.lite.configuration.DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS
import com.malinskiy.marathon.lite.configuration.DEFAULT_EXCLUDES_SERIAL_REGEXES
import com.malinskiy.marathon.lite.configuration.DEFAULT_EXECUTION_TIMEOUT_MILLIS
import com.malinskiy.marathon.lite.configuration.DEFAULT_FALLBACK_TO_SCREENSHOTS
import com.malinskiy.marathon.lite.configuration.DEFAULT_FILTERING_CONFIGURATION
import com.malinskiy.marathon.lite.configuration.DEFAULT_FLAKINESS_STRATEGY
import com.malinskiy.marathon.lite.configuration.DEFAULT_IGNORE_FAILURES
import com.malinskiy.marathon.lite.configuration.DEFAULT_INCLUDES_SERIAL_REGEXES
import com.malinskiy.marathon.lite.configuration.DEFAULT_INIT_TIMEOUT_MILLIS
import com.malinskiy.marathon.lite.configuration.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.lite.configuration.DEFAULT_IS_CODE_COVERAGE_ENABLED
import com.malinskiy.marathon.lite.configuration.DEFAULT_OUTPUT_TIMEOUT_MILLIS
import com.malinskiy.marathon.lite.configuration.DEFAULT_POOLING_STRATEGY
import com.malinskiy.marathon.lite.configuration.DEFAULT_RETRY_STRATEGY
import com.malinskiy.marathon.lite.configuration.DEFAULT_SHARDING_STRATEGY
import com.malinskiy.marathon.lite.configuration.DEFAULT_SORTING_STRATEGY
import com.malinskiy.marathon.lite.configuration.DEFAULT_STRICT_MODE
import com.malinskiy.marathon.lite.configuration.DEFAULT_TEST_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.lite.configuration.DEFAULT_TEST_CLASS_REGEXES
import com.malinskiy.marathon.lite.configuration.DEFAULT_UNCOMPLETED_TEST_RETRY_QUOTA
import com.malinskiy.marathon.lite.configuration.DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
import com.malinskiy.marathon.lite.configuration.FilteringConfiguration
import com.malinskiy.marathon.lite.configuration.ScreenRecordingPolicy
import com.malinskiy.marathon.lite.configuration.android.AllureConfiguration
import com.malinskiy.marathon.lite.configuration.android.FileSyncConfiguration
import com.malinskiy.marathon.lite.configuration.android.ScreenRecordConfiguration
import com.malinskiy.marathon.lite.configuration.android.SerialStrategy
import com.malinskiy.marathon.lite.configuration.android.TimeoutConfiguration
import com.malinskiy.marathon.lite.configuration.android.VendorType
import com.malinskiy.marathon.lite.configuration.strategies.BatchingStrategy
import com.malinskiy.marathon.lite.configuration.strategies.FlakinessStrategy
import com.malinskiy.marathon.lite.configuration.strategies.PoolingStrategy
import com.malinskiy.marathon.lite.configuration.strategies.RetryStrategy
import com.malinskiy.marathon.lite.configuration.strategies.ShardingStrategy
import com.malinskiy.marathon.lite.configuration.strategies.SortingStrategy
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class MarathonExtension @Inject constructor(
    objects: ObjectFactory
) {
    var name: Property<String> = objects.property()

    var vendor: Property<VendorType> = objects.property()
    var bugsnag: Property<Boolean> = objects.property()

    var analyticsConfiguration: Property<AnalyticsConfiguration> = objects.property()

    var poolingStrategy: Property<PoolingStrategy> = objects.property()
    var shardingStrategy: Property<ShardingStrategy> = objects.property()
    var sortingStrategy: Property<SortingStrategy> = objects.property()
    var batchingStrategy: Property<BatchingStrategy> = objects.property()
    var flakinessStrategy: Property<FlakinessStrategy> = objects.property()
    var retryStrategy: Property<RetryStrategy> = objects.property()
    var filteringConfiguration: Property<FilteringConfiguration> = objects.property()

    var baseOutputDir: Property<String> = objects.property()

    var ignoreFailures: Property<Boolean> = objects.property()
    var isCodeCoverageEnabled: Property<Boolean> = objects.property()
    var fallbackToScreenshots: Property<Boolean> = objects.property()
    var strictMode: Property<Boolean> = objects.property()
    var uncompletedTestRetryQuota: Property<Int> = objects.property()

    var testClassRegexes: Property<Collection<String>> = objects.property()
    var includeSerialRegexes: Property<Collection<String>> = objects.property()
    var excludeSerialRegexes: Property<Collection<String>> = objects.property()

    var testBatchTimeoutMillis: Property<Long> = objects.property()
    var testOutputTimeoutMillis: Property<Long> = objects.property()
    var debug: Property<Boolean> = objects.property()

    var screenRecordingPolicy: Property<ScreenRecordingPolicy> = objects.property()

    var applicationPmClear: Property<Boolean> = objects.property()
    var testApplicationPmClear: Property<Boolean> = objects.property()
    var adbInitTimeout: Property<Int> = objects.property()
    var installOptions: Property<String> = objects.property()
    var serialStrategy: Property<SerialStrategy> = objects.property()

    var screenRecordConfiguration: Property<ScreenRecordConfiguration> = objects.property()

    var analyticsTracking: Property<Boolean> = objects.property()

    var deviceInitializationTimeoutMillis: Property<Long> = objects.property()
    var waitForDevicesTimeoutMillis: Property<Long> = objects.property()

    var allureConfiguration: Property<AllureConfiguration> = objects.property()
    var timeoutConfiguration: Property<TimeoutConfiguration> = objects.property()
    var fileSyncConfiguration: Property<FileSyncConfiguration> = objects.property()

    //Android specific for now
    var autoGrantPermission: Property<Boolean> = objects.property()
    var instrumentationArgs: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)

    init {
        name.convention("marathon")
        baseOutputDir.set("")
        vendor.convention(VendorType.DDMLIB)
        bugsnag.convention(true)

        analyticsConfiguration.convention(DEFAULT_ANALYTICS_CONFIGURATION)

        poolingStrategy.convention(DEFAULT_POOLING_STRATEGY)
        shardingStrategy.convention(DEFAULT_SHARDING_STRATEGY)
        sortingStrategy.convention(DEFAULT_SORTING_STRATEGY)
        batchingStrategy.convention(DEFAULT_BATCHING_STRATEGY)
        flakinessStrategy.convention(DEFAULT_FLAKINESS_STRATEGY)
        retryStrategy.convention(DEFAULT_RETRY_STRATEGY)
        filteringConfiguration.convention(DEFAULT_FILTERING_CONFIGURATION)

        // TODO: fix it later
//        baseOutputDir: Property<String> = objects.property()

        ignoreFailures.convention(DEFAULT_IGNORE_FAILURES)
        isCodeCoverageEnabled.convention(DEFAULT_IS_CODE_COVERAGE_ENABLED)
        fallbackToScreenshots.convention(DEFAULT_FALLBACK_TO_SCREENSHOTS)
        strictMode.convention(DEFAULT_STRICT_MODE)
        uncompletedTestRetryQuota.convention(DEFAULT_UNCOMPLETED_TEST_RETRY_QUOTA)

        testClassRegexes.convention(DEFAULT_TEST_CLASS_REGEXES)
        includeSerialRegexes.convention(DEFAULT_INCLUDES_SERIAL_REGEXES)
        excludeSerialRegexes.convention(DEFAULT_EXCLUDES_SERIAL_REGEXES)

        testBatchTimeoutMillis.convention(DEFAULT_EXECUTION_TIMEOUT_MILLIS)
        testOutputTimeoutMillis.convention(DEFAULT_OUTPUT_TIMEOUT_MILLIS)
        debug.convention(true)

        screenRecordingPolicy.convention(ScreenRecordingPolicy.ON_FAILURE)

        applicationPmClear.convention(DEFAULT_APPLICATION_PM_CLEAR)
        testApplicationPmClear.convention(DEFAULT_TEST_APPLICATION_PM_CLEAR)
        adbInitTimeout.convention(DEFAULT_INIT_TIMEOUT_MILLIS)
        installOptions.convention(DEFAULT_INSTALL_OPTIONS)
        serialStrategy.convention(SerialStrategy.AUTOMATIC)

        screenRecordConfiguration.convention(ScreenRecordConfiguration())

        analyticsTracking.convention(false)

        deviceInitializationTimeoutMillis.convention(DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS)
        waitForDevicesTimeoutMillis.convention(DEFAULT_WAIT_FOR_DEVICES_TIMEOUT)

        allureConfiguration.convention(AllureConfiguration())
        timeoutConfiguration.convention(TimeoutConfiguration())
        fileSyncConfiguration.convention(FileSyncConfiguration())

        autoGrantPermission.convention(DEFAULT_AUTO_GRANT_PERMISSION)
        instrumentationArgs.convention(emptyMap())
    }
}
