package com.malinskiy.marathon.analytics.internal.sub

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.bill.DeviceBill
import com.malinskiy.marathon.test.toTestName
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Events are sorted by timestamp
 */
data class ExecutionReport(
    val deviceConnectedEvents: List<DeviceConnectedEvent>,
    val deviceDisconnectedEvents: List<DeviceDisconnectedEvent>,
    val devicePreparingEvents: List<DevicePreparingEvent>,
    val deviceProviderPreparingEvent: List<DeviceProviderPreparingEvent>,
    val testEvents: List<TestEvent>,
    val defaultStart: Instant,
) {
    private val logger = MarathonLogging.logger {}

    val summary: Summary by lazy {
        val pools = deviceConnectedEvents.map { it.poolId }.distinct()
        val poolsSummary: List<PoolSummary> = pools.map { compilePoolSummary(it) }
        Summary(poolsSummary)
    }

    val result: Boolean by lazy { summary.pools.map { it.failed.isEmpty() }.reduceOrNull { acc, b -> acc && b } ?: true }
    val flakiness: Duration by lazy {
        summary.pools.mapNotNull {
            val poolFlakiness = it.rawDurationMillis - it.durationMillis
            if (poolFlakiness < 0) {
                logger.warn { "Pool ${it.poolId.name} has negative flakiness" }
                null
            } else {
                Duration.of(poolFlakiness, ChronoUnit.MILLIS)
            }
        }.fold(Duration.ZERO) { acc, duration -> acc + duration}
    }
    val duration: Duration by lazy { Duration.between(defaultStart, Instant.now()) }

    val bills: List<DeviceBill> by lazy {
        val starts = mutableMapOf<String, Instant>()
        val devices = deviceConnectedEvents.associateBy { it.device.serialNumber }.mapValues { it.value.device }
        val pools = deviceConnectedEvents.associateBy { it.device.serialNumber }.mapValues { it.value.poolId }

        devicePreparingEvents.forEach {
            if (starts.contains(it.serialNumber)) {
                //Only replace if event finish is before current one
                if (starts[it.serialNumber]?.isAfter(it.finish) == true) {
                    starts[it.serialNumber] = it.finish
                }
            } else {
                starts[it.serialNumber] = it.finish
            }
        }

        val testEventsByDeviceSerial = testEvents.groupBy { it.device.serialNumber }
        val ends = testEventsByDeviceSerial.mapValues { deviceEvents ->
            deviceEvents.value.maxByOrNull { it.instant }?.instant
        }

        val serials = starts.keys + ends.keys
        serials.mapNotNull {
            val start = starts[it] ?: return@mapNotNull null
            val end = ends[it] ?: return@mapNotNull null
            val info = devices[it]
            val pool = pools[it]
            if (info != null && pool != null) {
                DeviceBill(info, pool, start, end, Duration.between(start, end))
            } else {
                logger.warn { "Failure to process device bill: missing timeline event" }
                null
            }
        }
    }

    private fun compilePoolSummary(poolId: DevicePoolId): PoolSummary {
        val devices = deviceConnectedEvents.filter { it.poolId == poolId }.map { it.device }.distinctBy { it.serialNumber }

        val poolTestEvents = testEvents.filter { it.poolId == poolId }
        val poolTestFinalEvents = poolTestEvents.filter { it.final }

        val tests = poolTestFinalEvents
            .map { it.testResult }
            .filter { it.status != TestStatus.INCOMPLETE }

        val passed = tests
            .filter { it.status == TestStatus.PASSED }
            .map { it.test }
            .toSet()

        val ignored = tests
            .filter {
                it.status == TestStatus.IGNORED
                    || it.status == TestStatus.ASSUMPTION_FAILURE
            }.map { it.test }
            .toSet()

        val failed = tests
            .filter {
                it.status != TestStatus.PASSED
                    && it.status != TestStatus.IGNORED
                    && it.status != TestStatus.ASSUMPTION_FAILURE
            }.map { it.test }
            .toSet()

        val duration = tests.filter { it.isTimeInfoAvailable }
            .sumOf { it.durationMillis() }

        val rawTests = poolTestEvents
            .map { it.testResult }
        val rawPassed = rawTests
            .filter { it.status == TestStatus.PASSED }
            .map { it.test }
        val rawIgnored = rawTests
            .filter {
                it.status == TestStatus.IGNORED
                    || it.status == TestStatus.ASSUMPTION_FAILURE
            }.map { it.test }
        val rawFailed = rawTests
            .filter { it.status == TestStatus.FAILURE }
            .map { it.test }
        val rawIncomplete = rawTests
            .filter { it.status == TestStatus.INCOMPLETE }
            .map { it.test }
        val rawDuration = rawTests
            //Incomplete tests mess up the calculations of time since their end time is 0 and duration is, hence, years
            //We filter here for unavailable time just to be safe
            .filter { it.isTimeInfoAvailable }.sumOf { it.durationMillis() }

        val retries = tests.map { result: TestResult ->
            Pair(result, poolTestEvents.filter { it.testResult.test == result.test && it.testResult !== result })
        }.toMap()

        return PoolSummary(
            poolId = poolId,
            tests = tests,
            retries = retries,
            passed = passed,
            ignored = ignored,
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

data class TestEvent(
    val instant: Instant,
    val poolId: DevicePoolId,
    val device: DeviceInfo,
    val testResult: TestResult,
    val final: Boolean
) : Event()

data class DeviceDisconnectedEvent(
    val instant: Instant,
    val poolId: DevicePoolId,
    val device: DeviceInfo
) : Event()
