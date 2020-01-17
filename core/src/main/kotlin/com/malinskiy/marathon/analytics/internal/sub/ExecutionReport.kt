package com.malinskiy.marathon.analytics.internal.sub

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test
import java.time.Instant

/**
 * Events are sorted by timestamp
 */
data class ExecutionReport(
    val deviceConnectedEvents: List<DeviceConnectedEvent>,
    val devicePreparingEvents: List<DevicePreparingEvent>,
    val deviceProviderPreparingEvent: List<DeviceProviderPreparingEvent>,
    val installCheckEvent: List<InstallationCheckEvent>,
    val installEvent: List<InstallationEvent>,
    val executeBatchEvent: List<ExecutingBatchEvent>,
    val cacheStoreEvent: List<CacheStoreEvent>,
    val cacheLoadEvent: List<CacheLoadEvent>,
    val testEvents: List<TestEvent>
) {
    val summary: Summary by lazy {
        val pools = deviceConnectedEvents.map { it.poolId }.distinct()
        val poolsSummary: List<PoolSummary> = pools.map { compilePoolSummary(it) }
        Summary(poolsSummary)
    }

    val allEvents: List<Event> by lazy {
        listOf(
            deviceConnectedEvents,
            devicePreparingEvents,
            devicePreparingEvents,
            installCheckEvent,
            installEvent,
            executeBatchEvent,
            cacheStoreEvent,
            cacheLoadEvent,
            testEvents
        )
            .flatten()
    }

    private fun compilePoolSummary(poolId: DevicePoolId): PoolSummary {
        val devices = deviceConnectedEvents.filter { it.poolId == poolId }.map { it.device }.distinctBy { it.serialNumber }

        val poolTestEvents = testEvents.filter { it.poolId == poolId }
        val poolTestFinalEvents = poolTestEvents.filter { it.final }

        val tests = poolTestFinalEvents
            .map { it.testResult }
            .filter { it.status != TestStatus.INCOMPLETE }

        val passed = tests.count { it.status == TestStatus.PASSED }
        val ignored = tests.count {
            it.status == TestStatus.IGNORED
                    || it.status == TestStatus.ASSUMPTION_FAILURE
        }
        val fromCache = tests.count { it.isFromCache }
        val failed = tests.count {
            it.status != TestStatus.PASSED
                    && it.status != TestStatus.IGNORED
                    && it.status != TestStatus.ASSUMPTION_FAILURE
        }
        val duration = tests.map { it.durationMillis() }.sum()

        val rawTests = poolTestEvents
            .map { it.testResult }
        val rawPassed = rawTests.count { it.status == TestStatus.PASSED }
        val rawIgnored = rawTests.count {
            it.status == TestStatus.IGNORED
                    || it.status == TestStatus.ASSUMPTION_FAILURE
        }
        val rawFailed = rawTests.count { it.status == TestStatus.FAILURE }
        val rawIncomplete = rawTests.count { it.status == TestStatus.INCOMPLETE }
        val rawDuration = rawTests.map { it.durationMillis() }.sum()

        return PoolSummary(
            poolId = poolId,
            tests = tests,
            passed = passed,
            ignored = ignored,
            fromCache = fromCache,
            failed = failed,
            flaky = 0,
            durationMillis = duration,
            devices = devices,
            rawPassed = rawPassed,
            rawFailed = rawFailed,
            rawIgnored = rawIgnored,
            rawIncomplete = rawIncomplete,
            rawDurationMillis = rawDuration
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

data class InstallationCheckEvent(
    val start: Instant,
    val finish: Instant,
    val serialNumber: String
) : Event()

data class InstallationEvent(
    val start: Instant,
    val finish: Instant,
    val serialNumber: String
) : Event()

data class ExecutingBatchEvent(
    val start: Instant,
    val finish: Instant,
    val serialNumber: String
) : Event()

data class CacheStoreEvent(
    val start: Instant,
    val finish: Instant,
    val test: Test
) : Event()

data class CacheLoadEvent(
    val start: Instant,
    val finish: Instant,
    val test: Test
) : Event()

data class TestEvent(
    val instant: Instant,
    val poolId: DevicePoolId,
    val device: DeviceInfo,
    val testResult: TestResult,
    val final: Boolean
) : Event()
