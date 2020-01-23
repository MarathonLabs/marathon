package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.analytics.external.MetricsProviderFactory
import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import com.malinskiy.marathon.analytics.external.influx.InfluxMetricsProvider
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.amshove.kluent.shouldBeInstanceOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

class MetricsProviderFactorySpek : Spek(
    {
        describe("test metrics provider") {
            it("should return noop metrics provider when analytics configuration is disabled") {
                val configuration = Configuration(
                    name = "",
                    outputDir = File(""),
                    analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics,
                    poolingStrategy = null,
                    shardingStrategy = null,
                    sortingStrategy = null,
                    batchingStrategy = null,
                    flakinessStrategy = null,
                    retryStrategy = null,
                    filteringConfiguration = null,
                    ignoreFailures = null,
                    isCodeCoverageEnabled = null,
                    fallbackToScreenshots = null,
                    strictMode = null,
                    uncompletedTestRetryQuota = null,
                    testClassRegexes = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testBatchTimeoutMillis = null,
                    testOutputTimeoutMillis = null,
                    debug = null,
                    screenRecordingPolicy = null,
                    vendorConfiguration = object : VendorConfiguration {
                        override fun testParser(): TestParser? = null
                        override fun deviceProvider(): DeviceProvider? = null
                        override fun logConfigurator(): MarathonLogConfigurator? = null
                        override fun preferableRecorderType(): DeviceFeature? = null
                    },
                    analyticsTracking = false
                )
                val factory = MetricsProviderFactory(configuration)
                val metricsProvider = factory.create()
                metricsProvider shouldBeInstanceOf NoOpMetricsProvider::class
            }

            it("should return noop metrics provider when influx analytics configuration is invalid") {
                val analyticsConfiguration = AnalyticsConfiguration.InfluxDbConfiguration(
                    "host",
                    "user",
                    "password",
                    "db",
                    AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
                )
                val configuration = Configuration(
                    name = "",
                    outputDir = File(""),
                    analyticsConfiguration = analyticsConfiguration,
                    poolingStrategy = null,
                    shardingStrategy = null,
                    sortingStrategy = null,
                    batchingStrategy = null,
                    flakinessStrategy = null,
                    retryStrategy = null,
                    filteringConfiguration = null,
                    ignoreFailures = null,
                    isCodeCoverageEnabled = null,
                    fallbackToScreenshots = null,
                    strictMode = null,
                    uncompletedTestRetryQuota = null,
                    testClassRegexes = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testBatchTimeoutMillis = null,
                    testOutputTimeoutMillis = null,
                    debug = null,
                    screenRecordingPolicy = null,
                    vendorConfiguration = object : VendorConfiguration {
                        override fun testParser(): TestParser? = null
                        override fun deviceProvider(): DeviceProvider? = null
                        override fun logConfigurator(): MarathonLogConfigurator? = null
                        override fun preferableRecorderType(): DeviceFeature? = null
                    },
                    analyticsTracking = false
                )
                val factory = MetricsProviderFactory(configuration)
                val metricsProvider = factory.create()
                metricsProvider shouldBeInstanceOf NoOpMetricsProvider::class
            }

            it("should return noop metrics provider when influx analytics configuration is invalid") {
                val analyticsConfiguration = AnalyticsConfiguration.InfluxDbConfiguration(
                    "http://influxdb.local",
                    "user",
                    "password",
                    "db",
                    AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
                )
                val configuration = Configuration(
                    name = "",
                    outputDir = File(""),
                    analyticsConfiguration = analyticsConfiguration,
                    poolingStrategy = null,
                    shardingStrategy = null,
                    sortingStrategy = null,
                    batchingStrategy = null,
                    flakinessStrategy = null,
                    retryStrategy = null,
                    filteringConfiguration = null,
                    ignoreFailures = null,
                    isCodeCoverageEnabled = null,
                    fallbackToScreenshots = null,
                    strictMode = null,
                    uncompletedTestRetryQuota = null,
                    testClassRegexes = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testBatchTimeoutMillis = null,
                    testOutputTimeoutMillis = null,
                    debug = null,
                    screenRecordingPolicy = null,
                    vendorConfiguration = object : VendorConfiguration {
                        override fun testParser(): TestParser? = null
                        override fun deviceProvider(): DeviceProvider? = null
                        override fun logConfigurator(): MarathonLogConfigurator? = null
                        override fun preferableRecorderType(): DeviceFeature? = null
                    },
                    analyticsTracking = false
                )
                val factory = MetricsProviderFactory(configuration)
                val metricsProvider = factory.create()
                metricsProvider shouldBeInstanceOf InfluxMetricsProvider::class
            }
        }
    })
