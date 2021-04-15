package com.malinskiy.marathon.lite


import com.malinskiy.marathon.lite.configuration.AllureConfiguration
import com.malinskiy.marathon.lite.configuration.AnalyticsConfiguration
import com.malinskiy.marathon.lite.configuration.BatchingStrategy
import com.malinskiy.marathon.lite.configuration.FileSyncConfiguration
import com.malinskiy.marathon.lite.configuration.FilteringConfiguration
import com.malinskiy.marathon.lite.configuration.FlakinessStrategy
import com.malinskiy.marathon.lite.configuration.PoolingStrategy
import com.malinskiy.marathon.lite.configuration.RetryStrategy
import com.malinskiy.marathon.lite.configuration.ScreenRecordConfiguration
import com.malinskiy.marathon.lite.configuration.ScreenRecordingPolicy
import com.malinskiy.marathon.lite.configuration.SerialStrategy
import com.malinskiy.marathon.lite.configuration.ShardingStrategy
import com.malinskiy.marathon.lite.configuration.SortingStrategy
import com.malinskiy.marathon.lite.configuration.TimeoutConfiguration
import com.malinskiy.marathon.lite.configuration.VendorType
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
    var autoGrantPermissions: Property<Boolean> = objects.property()
    var instrumentationArgs: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)

    init {
        name.convention("marathon")
        baseOutputDir.set("")
        vendor.convention(VendorType.ADAM)
        bugsnag.convention(false)

//        // TODO: fix it later
////        baseOutputDir: Property<String> = objects.property()
//
        instrumentationArgs.convention(emptyMap())
    }
}
