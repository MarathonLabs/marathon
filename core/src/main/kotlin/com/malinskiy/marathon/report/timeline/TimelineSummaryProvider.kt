package com.malinskiy.marathon.report.timeline

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.extension.escape
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.toClassName

class TimelineSummaryProvider {
    val logger = MarathonLogging.logger(TimelineSummaryProvider::class.java.simpleName)

    private fun parseData(report: ExecutionReport): List<Data> {
        val perTestAttempts: Map<String, MutableList<TestEvent>> = mutableMapOf<String, MutableList<TestEvent>>().apply {
            report.testEvents.groupByTo(this) { "${it.testResult.test.clazz}.${it.testResult.test.method}" }
                .values.forEach { it.sortBy { evt -> evt.testResult.startTime } }
        }
        val eventAttemptIndex: Map<TestEvent, Int> = perTestAttempts.values.flatMap { events ->
            events.mapIndexed { index, event -> event to index }
        }.toMap()

        val testData = report.testEvents.map { convertToData(it, eventAttemptIndex[it] ?: 0) }

        val preparingData = report.devicePreparingEvents.map {
            Data(
                testName = MetricType.DEVICE_PREPARE.name,
                metricType = MetricType.DEVICE_PREPARE,
                startDate = it.start.toEpochMilli(),
                endDate = it.finish.toEpochMilli(),
                expectedValue = 0.0,
                variance = 0.0,
            )
        }

        val providerData = report.deviceProviderPreparingEvent.map {
            Data(
                testName = MetricType.DEVICE_PROVIDER_INIT.name,
                metricType = MetricType.DEVICE_PROVIDER_INIT,
                startDate = it.start.toEpochMilli(),
                endDate = it.finish.toEpochMilli(),
                expectedValue = 0.0,
                variance = 0.0,
            )
        }

        return (testData + preparingData + providerData).sortedBy { it.startDate }
    }

    private fun convertToData(event: TestEvent, attemptIndex: Int): Data {
        val preparedTestName = "${event.testResult.test.clazz}.${event.testResult.test.method}"
        // Reproduce the filename convention `HtmlSummaryReporter` uses so
        // consumers can build the same `pools/<pool>/<device>/<name>.html`
        // href the pool list emits — no extra lookup table required.
        val filename = "${event.testResult.test.toClassName()}.${event.testResult.test.method}".escape().safePathLength() + ".html"
        return Data(
            testName = preparedTestName,
            metricType = event.testResult.status.toMetricType(),
            startDate = event.testResult.startTime,
            endDate = event.testResult.endTime,
            expectedValue = 0.0,
            variance = 0.0,
            batchId = event.testResult.testBatchId,
            attemptIndex = attemptIndex,
            poolId = event.poolId.name,
            testFilename = filename,
            deviceSerial = event.device.safeSerialNumber,
        )
    }

    private fun calculateExecutionStats(data: List<Data>): ExecutionStats {
        return ExecutionStats(calculateIdle(data), calculateAverageExecutionTime(data))
    }

    private fun calculateAverageExecutionTime(data: List<Data>): Long {
        if (data.isEmpty()) return 0L
        return data.map { calculateDuration(it) }.average().toLong()
    }

    private fun calculateDuration(a: Data): Long = a.endDate - a.startDate

    private fun calculateIdle(data: List<Data>): Long =
        data.windowed(2, 1).fold(0L) { acc, list -> acc + (list[1].startDate - list[0].endDate) }

    private fun aggregateExecutionStats(list: List<Measure>): ExecutionStats {
        val summaryIdle = list.sumOf { it.executionStats.idleTimeMillis }
        val avgTestExecutionTime = list.map { it.executionStats.averageTestExecutionTimeMillis }
            .let { if (it.isEmpty()) 0L else it.average().toLong() }
        return ExecutionStats(summaryIdle, avgTestExecutionTime)
    }

    fun generate(executionReport: ExecutionReport): TimelineExecutionResult {
        val passedTestCount = executionReport.testEvents.count { it.testResult.isSuccess }
        val failedTests = executionReport.testEvents.count { !it.testResult.isSuccess }
        val ignoredTests = executionReport.testEvents.count { it.testResult.isIgnored }

        val deviceConnectedEvents = executionReport.deviceConnectedEvents.groupBy { it.device.serialNumber }
        val deviceDisconnectedEvents = executionReport.deviceDisconnectedEvents.groupBy { it.device.serialNumber }
        val devicePreparingEvent = executionReport.devicePreparingEvents.groupBy { it.serialNumber }
        val deviceProviderPreparingEvents = executionReport.deviceProviderPreparingEvent.groupBy { it.serialNumber }
        val testEvents = executionReport.testEvents.groupBy { it.device.serialNumber }

        val keys = deviceConnectedEvents.keys + devicePreparingEvent.keys +
            deviceProviderPreparingEvents.keys + testEvents.keys

        val reports = keys.associateWith { key ->
            ExecutionReport(
                deviceConnectedEvents = deviceConnectedEvents[key] ?: emptyList(),
                deviceDisconnectedEvents = deviceDisconnectedEvents[key] ?: emptyList(),
                devicePreparingEvents = devicePreparingEvent[key] ?: emptyList(),
                deviceProviderPreparingEvent = deviceProviderPreparingEvents[key] ?: emptyList(),
                testEvents = testEvents[key] ?: emptyList(),
            )
        }

        val deviceBySerial: Map<String, DeviceInfo> =
            executionReport.deviceConnectedEvents.associateBy({ it.device.serialNumber }, { it.device })

        val measures = reports.map { (serial, subReport) ->
            val data = parseData(subReport)
            Measure(
                measure = serial,
                executionStats = calculateExecutionStats(data),
                data = data,
                device = deviceBySerial[serial]?.toTimelineDevice(),
            )
        }

        val executionStats = aggregateExecutionStats(measures)
        return TimelineExecutionResult(
            passedTests = passedTestCount,
            failedTests = failedTests,
            ignoredTests = ignoredTests,
            executionStats = executionStats,
            measures = measures,
        )
    }

    private fun DeviceInfo.toTimelineDevice(): TimelineDevice = TimelineDevice(
        serial = safeSerialNumber,
        modelName = model,
        manufacturer = manufacturer,
        osVersion = operatingSystem.version,
        osMajor = operatingSystem.version.substringBefore('.').toIntOrNull(),
    )

    // Duplicate of the private helper in HtmlSummaryReporter — kept small so
    // this cross-module dep on filename derivation stays local rather than
    // pulling core::report.html into the timeline module.
    private fun String.safePathLength(): String =
        if (length >= 128) substring(0 until 128) else this

    private fun TestStatus.toMetricType() = when (this) {
        TestStatus.FAILURE -> MetricType.FAILURE
        TestStatus.PASSED -> MetricType.PASSED
        TestStatus.IGNORED -> MetricType.IGNORED
        TestStatus.INCOMPLETE -> MetricType.INCOMPLETE
        TestStatus.ASSUMPTION_FAILURE -> MetricType.ASSUMPTION_FAILURE
    }
}
