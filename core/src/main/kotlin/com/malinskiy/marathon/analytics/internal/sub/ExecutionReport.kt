package com.malinskiy.marathon.analytics.internal.sub

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import java.time.Instant

/**
 * Events are sorted by timestamp
 */
data class ExecutionReport(
    val deviceConnectedEvents: List<DeviceConnectedEvent>,
    val devicePreparingEvents: List<DevicePreparingEvent>,
    val deviceProviderPreparingEvent: List<DeviceProviderPreparingEvent>,
    val testEvents: List<TestEvent>
) {
    val summary: Summary by lazy {
        val pools = deviceConnectedEvents.map { it.poolId }.distinct()
        val poolsSummary: List<PoolSummary> = pools.map { compilePoolSummary(it) }
        Summary(poolsSummary)
    }

    private fun compilePoolSummary(poolId: DevicePoolId): PoolSummary {
        val devices = deviceConnectedEvents.filter { it.poolId == poolId }.map { it.device }.distinctBy { it.serialNumber }

        val tests = testEvents.filter { it.poolId == poolId }
            .map { it.testResult }
            .filter { it.status != TestStatus.INCOMPLETE }

        val passed = tests.count { it.status == TestStatus.PASSED }
        val ignored = tests.count {
            it.status == TestStatus.IGNORED
                    || it.status == TestStatus.ASSUMPTION_FAILURE
        }
        val failed = tests.count {
            it.status != TestStatus.PASSED
                    && it.status != TestStatus.IGNORED
                    && it.status != TestStatus.ASSUMPTION_FAILURE
        }
        val duration = tests.sumByDouble { it.durationMillis() * 1.0 }.toLong()
        return PoolSummary(
            poolId = poolId,
            tests = tests,
            passed = passed,
            ignored = ignored,
            failed = failed,
            flaky = 0,
            durationMillis = duration,
            devices = devices
        )
    }
}

sealed class Event

data class DeviceConnectedEvent(
    val instant: Instant,
    val poolId: DevicePoolId,
    val device: DeviceInfo
) : Event()

data class DevicePreparingEvent(
    val start: Instant,
    val finish: Instant,
    val serialNumber: String
) : Event()

data class DeviceProviderPreparingEvent(
    val start: Instant,
    val finish: Instant,
    val serialNumber: String
) : Event()

data class TestEvent(
    val instant: Instant,
    val poolId: DevicePoolId,
    val device: DeviceInfo,
    val testResult: TestResult,
    val final: Boolean
) : Event()