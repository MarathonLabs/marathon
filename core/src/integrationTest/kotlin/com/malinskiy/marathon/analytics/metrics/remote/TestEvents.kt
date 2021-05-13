package com.malinskiy.marathon.analytics.metrics.remote

import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceStub
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.generateTest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

val test = generateTest()

fun getTestEvents(): List<TestEvent> {
    val instant = Instant.now()
    val deviceInfo = DeviceStub().toDeviceInfo()
    return listOf(
        creteTestEvent(
            device = deviceInfo,
            duration = 1_000,
            status = TestStatus.PASSED,
            whenWasSent = instant.minus(1, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 2_000,
            status = TestStatus.PASSED,
            whenWasSent = instant.minus(10, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 3_000,
            status = TestStatus.PASSED,
            whenWasSent = instant.minus(20, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 4_000,
            status = TestStatus.PASSED,
            whenWasSent = instant.minus(30, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 5_000,
            status = TestStatus.PASSED,
            whenWasSent = instant.minus(40, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 6_000,
            status = TestStatus.FAILURE,
            whenWasSent = instant.minus(60, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 7_000,
            status = TestStatus.FAILURE,
            whenWasSent = instant.minus(70, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 8_000,
            status = TestStatus.FAILURE,
            whenWasSent = instant.minus(80, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 9_000,
            status = TestStatus.FAILURE,
            whenWasSent = instant.minus(90, ChronoUnit.MINUTES)
        ),
        creteTestEvent(
            device = deviceInfo,
            duration = 10_000,
            status = TestStatus.FAILURE,
            whenWasSent = instant.minus(100, ChronoUnit.MINUTES)
        )
    )
}

private fun creteTestEvent(device: DeviceInfo, duration: Long, status: TestStatus, whenWasSent: Instant) = TestEvent(
    whenWasSent,
    DevicePoolId("omni"),
    device,
    TestResult(
        test = test,
        device = device,
        status = status,
        startTime = whenWasSent.minusMillis(duration).toEpochMilli(),
        endTime = whenWasSent.toEpochMilli(),
        testBatchId = UUID.randomUUID().toString()
    ),
    true
)
