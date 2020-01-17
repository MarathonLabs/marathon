package com.malinskiy.marathon.report.timeline

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging

class TimelineSummaryProvider {
    val logger = MarathonLogging.logger(TimelineSummaryProvider::class.java.simpleName)

    private fun parseData(report: ExecutionReport): List<Data> {
        val testData = report.testEvents.map { convertToData(it) }

        val preparingData = report.devicePreparingEvents.map {
            Data(MetricType.DEVICE_PREPARE.name, MetricType.DEVICE_PREPARE, it.start.toEpochMilli(), it.finish.toEpochMilli(), 0.0, 0.0)
        }

        val providerData = report.deviceProviderPreparingEvent.map {
            Data(
                MetricType.DEVICE_PROVIDER_INIT.name,
                MetricType.DEVICE_PROVIDER_INIT,
                it.start.toEpochMilli(),
                it.finish.toEpochMilli(),
                0.0,
                0.0
            )
        }

        return (testData + preparingData + providerData).sortedBy { it.startDate }
    }

    private fun convertToData(event: TestEvent): Data {
        val preparedTestName = "${event.testResult.test.clazz}.${event.testResult.test.method}"
        val testMetric = getTestMetric(event)
        return createData(event, event.testResult.status, preparedTestName, testMetric)
    }

    data class TestMetric(val expectedValue: Double, val variance: Double)

    private fun createData(event: TestEvent, status: TestStatus, preparedTestName: String, testMetric: TestMetric): Data {
        return Data(
            preparedTestName,
            status.toMetricType(),
            event.testResult.startTime,
            event.testResult.endTime,
            testMetric.expectedValue, testMetric.variance
        )
    }

    private fun getTestMetric(execution: TestEvent): TestMetric {
        //TODO add real data
        return TestMetric(0.0, 0.0)
    }

    private fun calculateExecutionStats(data: List<Data>): ExecutionStats {
        return ExecutionStats(calculateIdle(data), calculateAverageExecutionTime(data))
    }

    private fun calculateAverageExecutionTime(data: List<Data>): Long {
        return data.map { this.calculateDuration(it) }.average().toLong()
    }

    private fun calculateDuration(a: Data): Long {
        return a.endDate - a.startDate
    }

    private fun calculateIdle(data: List<Data>): Long {
        return data.windowed(2, 1).fold(0L, { acc, list ->
            acc + (list[1].startDate - list[0].endDate)
        })
    }

    private fun aggregateExecutionStats(list: List<Measure>): ExecutionStats {
        val summaryIdle = list
            .map { it.executionStats.idleTimeMillis }
            .sum()
        val avgTestExecutionTime = list
            .map { it.executionStats.averageTestExecutionTimeMillis }
            .average()
            .toLong()
        return ExecutionStats(summaryIdle, avgTestExecutionTime)
    }

    fun generate(executionReport: ExecutionReport): TimelineExecutionResult {
        val passedTestCount = executionReport.testEvents.count { it.testResult.isSuccess }
        val failedTests = executionReport.testEvents.count { !it.testResult.isSuccess }
        val ignoredTests = executionReport.testEvents.count { it.testResult.isIgnored }

        val deviceConnectedEvents = executionReport.deviceConnectedEvents.groupBy { it.device.serialNumber }
        val devicePreparingEvent = executionReport.devicePreparingEvents.groupBy { it.serialNumber }
        val deviceProviderPreparingEvents = executionReport.deviceProviderPreparingEvent.groupBy { it.serialNumber }
        val testEvents = executionReport.testEvents.groupBy { it.device.serialNumber }

        val keys = (deviceConnectedEvents.keys + devicePreparingEvent.keys + deviceProviderPreparingEvents.keys + testEvents.keys)

        val reports = keys.map { key ->
            key to ExecutionReport(
                deviceConnectedEvents = deviceConnectedEvents[key] ?: emptyList(),
                devicePreparingEvents = devicePreparingEvent[key] ?: emptyList(),
                deviceProviderPreparingEvent = deviceProviderPreparingEvents[key] ?: emptyList(),
                testEvents = testEvents[key] ?: emptyList(),
                installCheckEvent = emptyList(),
                installEvent = emptyList(),
                executeBatchEvent = emptyList(),
                cacheLoadEvent = emptyList(),
                cacheStoreEvent = emptyList()
            )
        }.toMap()

        val measures = reports.map {
            val serialNumber = it.key
            val data = parseData(it.value)
            Measure(serialNumber, calculateExecutionStats(data), data)
        }

        val executionStats = aggregateExecutionStats(measures)
        return TimelineExecutionResult(passedTestCount, failedTests, ignoredTests, executionStats, measures)
    }

    private fun TestStatus.toMetricType() = when (this) {
        TestStatus.FAILURE -> MetricType.FAILURE
        TestStatus.PASSED -> MetricType.PASSED
        TestStatus.IGNORED -> MetricType.IGNORED
        TestStatus.INCOMPLETE -> MetricType.INCOMPLETE
        TestStatus.ASSUMPTION_FAILURE -> MetricType.ASSUMPTION_FAILURE
    }
}

