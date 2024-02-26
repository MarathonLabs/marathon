package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.strategy.ExecutionMode
import com.malinskiy.marathon.config.strategy.ExecutionStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.progress.PoolProgressAccumulator
import com.malinskiy.marathon.extension.toRetryStrategy
import com.malinskiy.marathon.generateTestResults
import com.malinskiy.marathon.generateTests
import com.malinskiy.marathon.report.getDevice
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.io.File

class FixedQuotaRetryStrategyTest {
    private val anySuccessConfig = Configuration.Builder(
        name = "",
        outputDir = File("")
    ).apply {
        vendorConfiguration = VendorConfiguration.StubVendorConfiguration
        debug = false
        analyticsTracking = false
        executionStrategy = ExecutionStrategyConfiguration(ExecutionMode.ANY_SUCCESS, fast = false)
    }.build()

    private val track = mock<Track>()

    @Test
    fun `total quota tests, total quota is 1`() {
        val strategy = RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration(totalAllowedRetryQuota = 1).toRetryStrategy()
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(10)
        val testResults = generateTestResults(tests)
        val accumulator = PoolProgressAccumulator(
            poolId,
            TestShard(tests),
            anySuccessConfig,
            track
        )

        strategy.process(poolId, testResults, TestShard(tests), accumulator).size shouldBe 1
    }

    @Test
    fun `total quota tests, total quota more than size of the input list`() {
        val strategy = RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration(totalAllowedRetryQuota = 10 + 1).toRetryStrategy()
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(10)
        val testResults = generateTestResults(tests)
        val accumulator = PoolProgressAccumulator(
            poolId,
            TestShard(tests),
            anySuccessConfig,
            track
        )
        strategy.process(poolId, testResults, TestShard(tests), accumulator).size shouldBe 10
    }

    @Test
    fun `flakiness tests, should return all tests if flakytests size = 0`() {
        val strategy = RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration().toRetryStrategy()
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(50)
        val testResults = generateTestResults(tests)
        val accumulator = PoolProgressAccumulator(
            poolId,
            TestShard(tests),
            anySuccessConfig,
            track
        )

        strategy.process(poolId, testResults, TestShard(tests), accumulator).size shouldBe 50
    }

    @Test
    fun `flakiness tests, should return 0 tests if flakiness strategy added 3 flaky tests per test`() {
        val strategy = RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration().toRetryStrategy()
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(50)
        val testResults = generateTestResults(tests)
        val accumulator = PoolProgressAccumulator(
            poolId,
            TestShard(tests),
            anySuccessConfig,
            track
        )

        strategy.process(
            poolId,
            testResults,
            TestShard(tests, flakyTests = tests + tests + tests),
            accumulator
        ).size shouldBe 0
    }

    @Test
    fun `should return 0 tests if test reached terminal state`() {
        val strategy = RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration().toRetryStrategy()
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(1)
        val testResults = generateTestResults(tests)
        val accumulator = PoolProgressAccumulator(
            poolId,
            TestShard(tests),
            anySuccessConfig,
            track
        )
        val deviceInfo = getDevice()

        val test = tests.first()
        accumulator.testStarted(deviceInfo, test)
        accumulator.testEnded(deviceInfo, testResults.first())

        strategy.process(
            poolId,
            testResults,
            TestShard(tests),
            accumulator
        ).size shouldBe 0
    }
}
