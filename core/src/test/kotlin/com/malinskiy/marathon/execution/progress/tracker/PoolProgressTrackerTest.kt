package com.malinskiy.marathon.execution.progress.tracker

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.TestVendorConfiguration
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class PoolProgressTrackerTest {
    val test = MarathonTest(
        pkg = "com.malinskiy.marathon",
        clazz = "SomeTest",
        method = "someMethod",
        metaProperties = emptyList()
    )

    private fun createConfiguration(strictMode: Boolean): Configuration {
        return Configuration(
            name = "",
            outputDir = File(""),
            analyticsConfiguration = null,
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
            strictMode = strictMode,
            uncompletedTestRetryQuota = null,
            testClassRegexes = null,
            includeSerialRegexes = null,
            excludeSerialRegexes = null,
            testBatchTimeoutMillis = null,
            testOutputTimeoutMillis = null,
            debug = false,
            screenRecordingPolicy = null,
            vendorConfiguration = TestVendorConfiguration(Mocks.TestParser.DEFAULT, StubDeviceProvider()),
            analyticsTracking = false,
            deviceInitializationTimeoutMillis = null
        )
    }

    @Test
    fun nonStrictMode_case1() {
        val tracker = PoolProgressTracker(createConfiguration(strictMode = false))
        tracker.testCountExpectation(1)
        tracker.testStarted(test)
        tracker.testPassed(test)
        tracker.testFailed(test)
        tracker.aggregateResult().shouldBeEqualTo(true)
        tracker.testPassed(test)
        tracker.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun strictMode_case1() {
        val tracker = PoolProgressTracker(createConfiguration(strictMode = true))
        tracker.testCountExpectation(1)
        tracker.testStarted(test)
        tracker.testPassed(test)
        tracker.testFailed(test)
        tracker.aggregateResult().shouldBeEqualTo(false)
        tracker.testPassed(test)
        tracker.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun all_incomplete() {
        val tracker = PoolProgressTracker(createConfiguration(strictMode = false)).apply {
            testCountExpectation(1)
        }
        tracker.aggregateResult() shouldBeEqualTo false
    }

    @Test
    fun withRetries() {
        val tracker = PoolProgressTracker(createConfiguration(strictMode = false))

        tracker.testCountExpectation(1)
        tracker.testStarted(test)
        tracker.testFailed(test)
        tracker.addTestRetries(1)
        tracker.testStarted(test)
        tracker.testPassed(test)
        tracker.aggregateResult().shouldBeEqualTo(true)
    }
}
