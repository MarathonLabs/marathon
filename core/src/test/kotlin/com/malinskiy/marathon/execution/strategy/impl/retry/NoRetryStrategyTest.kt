package com.malinskiy.marathon.execution.strategy.impl.retry

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.strategy.ExecutionMode
import com.malinskiy.marathon.config.strategy.ExecutionStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.progress.PoolProgressAccumulator
import com.malinskiy.marathon.generateTestResults
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBeEmpty
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.io.File

class NoRetryStrategyTest {
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
    fun `should return empty list`() {
        val tests = generateTests(50)
        val testResults = generateTestResults(tests)
        val strategy = NoRetryStrategy()
        val devicePoolId = DevicePoolId("devicePoolId")
        val testShard = TestShard(tests)
        val accumulator = PoolProgressAccumulator(
            devicePoolId,
            TestShard(tests),
            anySuccessConfig,
            track
        )

        val result = strategy.process(devicePoolId, testResults, testShard, accumulator)
        result.shouldBeEmpty()
    }
}
