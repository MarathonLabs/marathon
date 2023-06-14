package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.strategy.ExecutionMode
import com.malinskiy.marathon.config.strategy.ExecutionStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.report.getDevice
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class PoolProgressAccumulatorTest {
    private val track = mock<Track>()
    private val analytics = mock<Analytics>()

    @BeforeEach
    fun `setup mocks`() {
        reset(track, analytics)
    }

    private val anySuccessConfig = Configuration.Builder(
        name = "",
        outputDir = File("")
    ).apply {
        vendorConfiguration = VendorConfiguration.StubVendorConfiguration
        debug = false
        analyticsTracking = false
        executionStrategy = ExecutionStrategyConfiguration(ExecutionMode.ANY_SUCCESS, fast = false)

    }.build()
    private val allSuccessConfig =
        anySuccessConfig.copy(executionStrategy = ExecutionStrategyConfiguration(ExecutionMode.ALL_SUCCESS, fast = false))
    private val successFastConfig =
        anySuccessConfig.copy(executionStrategy = ExecutionStrategyConfiguration(ExecutionMode.ANY_SUCCESS, fast = true))
    private val failFastConfig =
        anySuccessConfig.copy(executionStrategy = ExecutionStrategyConfiguration(ExecutionMode.ALL_SUCCESS, fast = true))
    val test = generateTest()
    private val poolId = DevicePoolId("test")
    private val device = getDevice()

    @Test
    fun `ANY_SUCCESS should pass on pass, failure, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(true)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS should pass on pass, pass, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(true)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS should pass on pass, fail, fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(true)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.FAILURE, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS should fail on no events`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            anySuccessConfig,
            track
        )
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ANY_SUCCESS should pass on retry, fail, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.retryTest(test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS should pass with runtime discovery`() {
        val testParameterized = generateTest(
            pkg = "com.malinskiy.marathon",
            clazz = "ParameterizedTest",
            method = "test",
            metaProperties = emptyList()
        )
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(testParameterized)),
            anySuccessConfig,
            track
        )
        val test0 = generateTest(
            pkg = "com.malinskiy.marathon",
            clazz = "ParameterizedTest",
            method = "test[0]",
            metaProperties = emptyList()
        )
        val test1 = generateTest(
            pkg = "com.malinskiy.marathon",
            clazz = "ParameterizedTest",
            method = "test[1]",
            metaProperties = emptyList()
        )

        reporter.testStarted(device, test0)
        reporter.testEnded(device, TestResult(test0, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test1)
        reporter.testEnded(device, TestResult(test1, device, "1", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testEnded(device, TestResult(testParameterized, device, "1", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS fast should pass on pass, fail, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test)),
            successFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(true)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS fast should fail on fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test)),
            successFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ANY_SUCCESS fast should fail on fail with many tries`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            successFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ANY_SUCCESS should pass on pass, incomplete, incomplete`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.INCOMPLETE, 1, 2), false)
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.INCOMPLETE, 2, 3), true)
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS should pass on pass, retry, fail, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.testStarted(device, test)
        reporter.retryTest(test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS should fail on fail, fail, fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.FAILURE, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ANY_SUCCESS should pass on fail, pass, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS fast should pass on fail, pass, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            successFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ANY_SUCCESS should fail on add retry, fail, add retry, fail, remove retry, fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.retryTest(test)
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)


        reporter.testStarted(device, test)
        reporter.retryTest(test)
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.removeTest(test, 1)
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.FAILURE, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `should fail on incomplete for a bunch of test tries without a single pass or fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            successFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.INCOMPLETE, 0, 1), false)
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.INCOMPLETE, 1, 2), false)
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.INCOMPLETE, 2, 3), true)
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `should fail on fail, incomplete, incomplete`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.INCOMPLETE, 1, 2), false)
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.INCOMPLETE, 2, 3), true)
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `should not change result after terminating to failed`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test)),
            anySuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.INCOMPLETE, 1, 2), false)
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.retryTest(test)
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.removeTest(test, 1)
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `should not change result after terminating to pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.INCOMPLETE, 1, 2), false)
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.retryTest(test)
        reporter.aggregateResult().shouldBeEqualTo(true)

        reporter.removeTest(test, 1)
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ALL_SUCCESS passing with multiple tries passing`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            failFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ALL_SUCCESS should fail on pass, pass, fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.FAILURE, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS fast should fail on pass, fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            failFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS should fail on pass, fail, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS should pass on pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ALL_SUCCESS should fail on fail with many test tries`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS should fail on first failure for a single test`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS fast should fail on failure for a many test tries`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            failFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS should fail on retry, pass, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test)),
            failFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.retryTest(test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ALL_SUCCESS should pass on remove retry, pass, pass with 3 test tries`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test, test)),
            failFastConfig,
            track
        )
        //Just removes one attempt. Now we have 3 tests to execute with all_success mode
        reporter.removeTest(test, 1)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)
        //2 tests left to execute

        val device2 = getDevice("2")
        //Concurrent device executing one of the tries
        reporter.testStarted(device, test)

        //Removes one that is not yet processed by the queue
        reporter.removeTest(test, 1)
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testEnded(device2, TestResult(test, device2, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(true)
    }

    @Test
    fun `ALL_SUCCESS should fail on pass, incomplete, incomplete`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.PASSED, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.INCOMPLETE, 1, 2), false)
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.INCOMPLETE, 2, 3), true)
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS should fail on fail, pass, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS fast should fail on fail, pass, pass`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            failFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.PASSED, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.PASSED, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS should fail on fail, fail, fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            allSuccessConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.FAILURE, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `ALL_SUCCESS fast should fail on fail, fail, fail`() {
        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test, test, test)),
            failFastConfig,
            track
        )
        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "1", TestStatus.FAILURE, 0, 1))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.aggregateResult().shouldBeEqualTo(false)

        reporter.testStarted(device, test)
        reporter.testEnded(device, TestResult(test, device, "3", TestStatus.FAILURE, 2, 3))
        reporter.aggregateResult().shouldBeEqualTo(false)
    }

    @Test
    fun `should report progress`() {
        val poolId = DevicePoolId("testpool")

        val test1 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method1", emptyList())
        val test2 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method2", emptyList())
        val test3 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method3", emptyList())

        val reporter = PoolProgressAccumulator(
            poolId,
            TestShard(listOf(test1, test2, test3)),
            anySuccessConfig,
            track
        )
        
        reporter.progress().shouldBeEqualTo(.0f)

        /**
         * test 1 passed
         */
        reporter.testStarted(device, test1)
        reporter.testEnded(device, TestResult(test1, device, "1", TestStatus.PASSED, 0, 1))
        reporter.progress().shouldBeEqualTo(1 / 3f)

        /**
         * test 2 failed
         */
        reporter.testStarted(device, test2)
        reporter.testEnded(device, TestResult(test2, device, "2", TestStatus.FAILURE, 1, 2))
        reporter.progress().shouldBeEqualTo(2 / 3f)

        /**
         * adding 4 retries for test2 and then test 2 passes once
         */
        reporter.retryTest(test2)
        reporter.retryTest(test2)
        reporter.retryTest(test2)
        reporter.retryTest(test2)
        reporter.progress().shouldBeEqualTo(2 / 7f)
        reporter.testStarted(device, test2)
        reporter.testEnded(device, TestResult(test2, device, "2", TestStatus.PASSED, 2, 3))
        reporter.progress().shouldBeEqualTo(3 / 7f)

        /**
         * 1 retry of test 2 fails
         */
        reporter.testStarted(device, test2)
        reporter.testEnded(device, TestResult(test2, device, "3", TestStatus.FAILURE, 3, 4))
        reporter.progress().shouldBeEqualTo(4 / 7f)

        /**
         * 1 retry of test 2 is ignored
         */
        reporter.testStarted(device, test2)
        reporter.testEnded(device, TestResult(test2, device, "4", TestStatus.IGNORED, 4, 5))
        reporter.progress().shouldBeEqualTo(5 / 7f)

        /**
         * removing one retry of test 2
         */
        reporter.removeTest(test2, 1)
        reporter.progress().shouldBeEqualTo(5 / 6f)

        /**
         * test 3 is ignored (assumption failure or just ignore)
         */
        reporter.testStarted(device, test3)
        reporter.testEnded(device, TestResult(test3, device, "5", TestStatus.IGNORED, 5, 6))
        val progress = reporter.progress()
        progress.shouldBeEqualTo(6 / 6f)
    }

    @Test
    fun shouldReportProgressForOnePoolWithRuntimeDiscovery() {
//        val poolId = DevicePoolId("testpool")
//
//        val test0 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method[0]", emptyList())
//        val test1 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method[1]", emptyList())
//        val test2 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method[2]", emptyList())
//
//        reporter.testCountExpectation(poolId, 1)
//        reporter.progress().shouldBeEqualTo(.0f)
//
//        /**
//         * [0] passed
//         */
//        reporter.testStarted(poolId, deviceInfo, test0)
//        reporter.testPassed(poolId, deviceInfo, test0)
//        reporter.progress().shouldBeEqualTo(1 / 1f)
//
//        /**
//         * [1] passed
//         */
//        reporter.testStarted(poolId, deviceInfo, test1)
//        reporter.testPassed(poolId, deviceInfo, test1)
//        reporter.progress().shouldBeEqualTo(2 / 1f)
//
//        /**
//         * [2] passed
//         */
//        reporter.testStarted(poolId, deviceInfo, test2)
//        reporter.testPassed(poolId, deviceInfo, test2)
//        reporter.progress().shouldBeEqualTo(3 / 1f)
//
//        val progress = reporter.progress()
//        progress.shouldBeEqualTo(6 / 6f)
    }
}
