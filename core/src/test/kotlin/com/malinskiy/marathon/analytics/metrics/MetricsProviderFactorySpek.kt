package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.amshove.kluent.shouldBeInstanceOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

class MetricsProviderFactorySpek : Spek({
    describe("test metrics provider") {
        it("should return noop metrics provider when analytics configuration is disabled") {
            val configuration = Configuration(name = "",
                    baseOutputDir = File(""),
                    outputDir = File(""),
                    applicationOutput = File(""),
                    testApplicationOutput = File(""),
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
                    testClassRegexes = null,
                    includedTestAnnotations = null,
                    excludedTestAnnotations = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testOutputTimeoutMillis = null,
                    debug = null,
                    testPackage = null,
                    autoGrantPermission = null,
                    vendorConfiguration = object : VendorConfiguration {})
            val factory = MetricsProviderFactory(configuration)
            val metricsProvider = factory.create()
            metricsProvider shouldBeInstanceOf NoOpMetricsProvider::class
        }
    }
})
