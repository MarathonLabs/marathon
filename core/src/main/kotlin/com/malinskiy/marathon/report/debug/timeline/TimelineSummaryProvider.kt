package com.malinskiy.marathon.report.debug.timeline

import com.malinskiy.marathon.analytics.tracker.device.DeviceInitMetric
import com.malinskiy.marathon.analytics.tracker.device.DeviceInitType
import com.malinskiy.marathon.analytics.tracker.device.InMemoryDeviceTracker
import com.malinskiy.marathon.analytics.tracker.local.RawTestResultTracker
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.PoolSummary
import com.malinskiy.marathon.report.Summary
import com.malinskiy.marathon.test.Test

class TimelineSummaryProvider(private val rawTestResultTracker: RawTestResultTracker) {
    val logger = MarathonLogging.logger(TimelineSummaryProvider::class.java.simpleName)

    private fun parseData(runs: List<RawTestResultTracker.RawTestRun>,
                          list: List<DeviceInitMetric>?): List<Data> {


        val testData = runs.map { this.convertToData(it) }


        val deviceData = list?.map {
            Data(it.type.name, it.type.toTimelineType(), it.startTime, it.finishTime, 0.0, 0.0)
        } ?: emptyList()
        return (testData + deviceData)
                .sortedBy { it.startDate }
    }

    private fun convertToData(testResult: RawTestResultTracker.RawTestRun): Data {
        val preparedTestName = "${testResult.clazz}.${testResult.method}"
        val testMetric = getTestMetric(testResult)
        return createData(testResult, testResult.status, preparedTestName, testMetric)
    }

    data class TestMetric(val expectedValue: Double, val variance: Double)

    private fun createData(execution: RawTestResultTracker.RawTestRun, status: TestStatus, preparedTestName: String, testMetric: TestMetric): Data {
        return Data(preparedTestName,
                status.toMetricType(),
                execution.timestamp,
                execution.timestamp + execution.duration,
                testMetric.expectedValue, testMetric.variance)
    }

    private fun getTestMetric(execution: RawTestResultTracker.RawTestRun): TestMetric {
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

    private fun extractIdentifiers(summary: PoolSummary): List<Test> {
        return summary.tests
                .filter { it.status == TestStatus.PASSED }
                .map { result -> result.test }
    }

    private fun passedTestCount(summary: Summary): Int {
        return summary.pools
                .flatMap { this.extractIdentifiers(it) }
                .distinct()
                .count()
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

    fun generate(): ExecutionResult {
        val passedTestCount = rawTestResultTracker.testResults.count { it.success }
        val failedTests = rawTestResultTracker.testResults.count { !it.success }
        val ignoredTests = rawTestResultTracker.testResults.count { it.ignored }

        val initMetrics = InMemoryDeviceTracker.metrics.groupBy { it.serialNumber }
        val measures = rawTestResultTracker.testResults.groupBy { it.deviceSerial }
                .map {
                    val data = parseData(it.value, initMetrics[it.key])
                    Measure(it.key, calculateExecutionStats(data), data)
                }

        val executionStats = aggregateExecutionStats(measures)
        return ExecutionResult(passedTestCount, failedTests, ignoredTests, executionStats, measures)
    }

    private fun TestStatus.toMetricType() = when (this) {
        TestStatus.FAILURE -> MetricType.FAILURE
        TestStatus.PASSED -> MetricType.PASSED
        TestStatus.IGNORED -> MetricType.IGNORED
        TestStatus.INCOMPLETE -> MetricType.INCOMPLETE
        TestStatus.ASSUMPTION_FAILURE -> MetricType.ASSUMPTION_FAILURE
    }
}

private fun DeviceInitType.toTimelineType(): MetricType {
    return when(this) {
        DeviceInitType.DEVICE_PROVIDER_INIT -> MetricType.DEVICE_PROVIDER_INIT
        DeviceInitType.DEVICE_PREPARE -> MetricType.DEVICE_PREPARE
    }
}
