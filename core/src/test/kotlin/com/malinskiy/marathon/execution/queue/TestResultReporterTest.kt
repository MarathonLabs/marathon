package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.createDeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.generateTest
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class TestResultReporterTest {
    private val track = mock<Track>()
    private val analytics = mock<Analytics>()

    @BeforeEach
    fun `setup mocks`() {
        reset(track, analytics)
    }

    private val defaultConfig = Configuration.Builder(
        name = "",
        outputDir = File("")
    ).apply {
        vendorConfiguration = VendorConfiguration.StubVendorConfiguration
        debug = false
        analyticsTracking = false
    }.build()
    private val strictConfig = defaultConfig.copy(strictMode = true)
    val test = generateTest()
    private val poolId = DevicePoolId("test")

    private fun filterDefault() = TestResultReporter(
        poolId,
        analytics,
        TestShard(listOf(test, test, test)),
        defaultConfig,
        track
    )

    private fun filterStrict() = TestResultReporter(
        poolId,
        analytics,
        TestShard(listOf(test, test, test)),
        strictConfig,
        track
    )

    private val deviceInfo = createDeviceInfo()

    @Test
    fun `default config, success - failure - failure, should report success`() {
        val filter = filterDefault()

        val r1 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 0, 1)
        val r2 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 2, 3)
        val r3 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 4, 5)

        filter.testFinished(deviceInfo, r1)
        filter.testFailed(deviceInfo, r2)
        filter.testFailed(deviceInfo, r3)

        inOrder(track) {
            verify(track).test(poolId, deviceInfo, r1, true)
            verify(track).test(poolId, deviceInfo, r2, false)
            verify(track).test(poolId, deviceInfo, r3, false)
            verifyNoMoreInteractions(track)
        }
    }

    @Test
    fun `default config, failure - failure - success, should report success`() {
        val filter = filterDefault()

        val r1 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 0, 1)
        val r2 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 2, 3)
        val r3 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 4, 5)

        filter.testFailed(deviceInfo, r1)
        filter.testFailed(deviceInfo, r2)
        filter.testFinished(deviceInfo, r3)

        inOrder(track) {
            verify(track).test(poolId, deviceInfo, r1, false)
            verify(track).test(poolId, deviceInfo, r2, false)
            verify(track).test(poolId, deviceInfo, r3, true)
            verifyNoMoreInteractions(track)
        }
    }

    @Test
    fun `strict config, success - failure - failure, should report failure`() {
        val filter = filterStrict()

        val r1 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 0, 1)
        val r2 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 2, 3)
        val r3 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 4, 5)

        filter.testFinished(deviceInfo, r1)
        filter.testFailed(deviceInfo, r2)
        filter.testFailed(deviceInfo, r3)

        inOrder(track) {
            verify(track).test(poolId, deviceInfo, r1, false)
            verify(track).test(poolId, deviceInfo, r2, true)
            verify(track).test(poolId, deviceInfo, r3, false)
            verifyNoMoreInteractions(track)
        }
    }

    @Test
    fun `strict config, failure - success - success, should report failure`() {
        val filter = filterStrict()

        val r1 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 0, 1)
        val r2 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 2, 3)
        val r3 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 4, 5)

        filter.testFailed(deviceInfo, r1)
        filter.testFinished(deviceInfo, r2)
        filter.testFinished(deviceInfo, r3)

        inOrder(track) {
            verify(track).test(poolId, deviceInfo, r1, true)
            verify(track).test(poolId, deviceInfo, r2, false)
            verify(track).test(poolId, deviceInfo, r3, false)
            verifyNoMoreInteractions(track)
        }
    }
}
