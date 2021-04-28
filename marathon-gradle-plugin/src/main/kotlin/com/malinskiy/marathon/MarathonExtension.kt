package com.malinskiy.marathon

import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.configuration.TimeoutConfiguration
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import java.io.Serializable
import javax.inject.Inject

open class MarathonExtension @Inject constructor(objects: ObjectFactory) : Serializable {
    val name: Property<String> = objects.property()

    val vendor: Property<VendorType> = objects.property()
    val bugsnag: Property<Boolean> = objects.property()

    val analyticsConfiguration: Property<AnalyticsConfig> = objects.property()

    val poolingStrategy: Property<PoolingStrategyConfiguration> = objects.property()
    val shardingStrategy: Property<ShardingStrategyConfiguration> = objects.property()
    val sortingStrategy: Property<SortingStrategyConfiguration> = objects.property()
    val batchingStrategy: Property<BatchingStrategyConfiguration> = objects.property()
    val flakinessStrategy: Property<FlakinessStrategyConfiguration> = objects.property()
    val retryStrategy: Property<RetryStrategyConfiguration> = objects.property()
    val filteringConfiguration: Property<FilteringPluginConfiguration> = objects.property()

    val baseOutputDir: Property<String> = objects.property()

    val ignoreFailures: Property<Boolean> = objects.property()
    val isCodeCoverageEnabled: Property<Boolean> = objects.property()
    val fallbackToScreenshots: Property<Boolean> = objects.property()
    val strictMode: Property<Boolean> = objects.property()
    val uncompletedTestRetryQuota: Property<Int> = objects.property()

    val testClassRegexes: Property<Collection<String>> = objects.property()
    val includeSerialRegexes: Property<Collection<String>> = objects.property()
    val excludeSerialRegexes: Property<Collection<String>> = objects.property()

    val testBatchTimeoutMillis: Property<Long> = objects.property()
    val testOutputTimeoutMillis: Property<Long> = objects.property()
    val debug: Property<Boolean> = objects.property()

    val screenRecordingPolicy: Property<ScreenRecordingPolicy> = objects.property()

    val applicationPmClear: Property<Boolean> = objects.property()
    val testApplicationPmClear: Property<Boolean> = objects.property()
    val adbInitTimeout: Property<Int> = objects.property()
    val installOptions: Property<String> = objects.property()
    val serialStrategy: Property<SerialStrategy> = objects.property()

    val screenRecordConfiguration: Property<ScreenRecordConfiguration> = objects.property()

    val analyticsTracking: Property<Boolean> = objects.property()

    val deviceInitializationTimeoutMillis: Property<Long> = objects.property()
    val waitForDevicesTimeoutMillis: Property<Long> = objects.property()

    val allureConfiguration: Property<AllureConfiguration> = objects.property()
    val timeoutConfiguration: Property<TimeoutConfiguration> = objects.property()
    val fileSyncConfiguration: Property<FileSyncConfiguration> = objects.property()

    //Android specific for now
    val autoGrantPermission: Property<Boolean> = objects.property()
    val instrumentationArgs: MapProperty<String, String> = objects.mapProperty()

    init {
        name.convention("Marathon")
        analyticsTracking.convention(false)
    }

    fun analytics(action: Action<AnalyticsConfig>) {
        analyticsConfiguration.set(AnalyticsConfig().also(action::execute))
    }

    fun batchingStrategy(action: Action<BatchingStrategyConfiguration>) {
        batchingStrategy.set(BatchingStrategyConfiguration().also(action::execute))
    }

    fun flakinessStrategy(action: Action<FlakinessStrategyConfiguration>) {
        flakinessStrategy.set(FlakinessStrategyConfiguration().also(action::execute))
    }

    fun poolingStrategy(action: Action<PoolingStrategyConfiguration>) {
        poolingStrategy.set(PoolingStrategyConfiguration().also(action::execute))
    }

    fun retryStrategy(action: Action<RetryStrategyConfiguration>) {
        retryStrategy.set(RetryStrategyConfiguration().also(action::execute))
    }

    fun shardingStrategy(action: Action<ShardingStrategyConfiguration>) {
        shardingStrategy.set(ShardingStrategyConfiguration().also(action::execute))
    }

    fun sortingStrategy(action: Action<SortingStrategyConfiguration>) {
        sortingStrategy.set(SortingStrategyConfiguration().also(action::execute))
    }

    fun filteringConfiguration(action: Action<FilteringPluginConfiguration>) {
        filteringConfiguration.set(FilteringPluginConfiguration().also(action::execute))
    }

    fun instrumentationArgs(action: Action<MutableMap<String, String>>) {
        instrumentationArgs.set(mutableMapOf<String, String>().also(action::execute))
    }

    fun allureConfiguration(action: Action<AllureConfiguration>) {
        allureConfiguration.set(AllureConfiguration().also(action::execute))
    }

    fun timeoutConfiguration(action: Action<TimeoutConfiguration>) {
        timeoutConfiguration.set(TimeoutConfiguration().also(action::execute))
    }

    fun fileSyncConfiguration(action: Action<FileSyncConfiguration>) {
        fileSyncConfiguration.set(FileSyncConfiguration().also(action::execute))
    }
}
