package com.malinskiy.marathon.lite

import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.configuration.TimeoutConfiguration
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
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

    var analyticsConfiguration: Property<AnalyticsConfig> = objects.property()

    var poolingStrategy: Property<PoolingStrategyConfiguration> = objects.property()
    var shardingStrategy: Property<ShardingStrategyConfiguration> = objects.property()
    var sortingStrategy: Property<SortingStrategyConfiguration> = objects.property()
    var batchingStrategy: Property<BatchingStrategyConfiguration> = objects.property()
    var flakinessStrategy: Property<FlakinessStrategyConfiguration> = objects.property()
    var retryStrategy: Property<RetryStrategyConfiguration> = objects.property()
    var filteringConfiguration: Property<FilteringPluginConfiguration> = objects.property()

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
}
