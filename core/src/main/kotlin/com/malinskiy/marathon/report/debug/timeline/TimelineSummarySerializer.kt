package com.malinskiy.marathon.report.debug.timeline

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.PoolSummary
import com.malinskiy.marathon.report.Summary
import com.malinskiy.marathon.report.internal.TestResultReporter
import com.malinskiy.marathon.test.Test

class TimelineSummarySerializer(private val testResultSerializer: TestResultReporter) {
    val logger = MarathonLogging.logger(TimelineSummarySerializer::class.java.simpleName)

    private fun prepareTestName(fullTestName: String): String {
        return fullTestName.substring(fullTestName.lastIndexOf('.') + 1)
    }

    private fun parseData(poolId: DevicePoolId, device: DeviceInfo): List<Data> {
        val executions = testResultSerializer.readTests(poolId, device)
        return executions.map { this.convertToData(it) }.sortedBy { it.startDate }
    }

    private fun convertToData(testResult: TestResult): Data {
        val preparedTestName = prepareTestName(testResult.test.toString())
        val testMetric = getTestMetric(testResult)
        return createData(testResult, testResult.status, preparedTestName, testMetric)
    }

    private fun TestStatus.toStatus(): Status = when (this) {
        TestStatus.PASSED -> Status.PASSED
        TestStatus.IGNORED -> Status.IGNORED
        TestStatus.FAILURE -> Status.FAILURE
        TestStatus.INCOMPLETE -> Status.INCOMPLETE
        TestStatus.ASSUMPTION_FAILURE -> Status.ASSUMPTION_FAILURE
    }

    data class TestMetric(val expectedValue: Double, val variance: Double)

    private fun createData(execution: TestResult, status: TestStatus, preparedTestName: String, testMetric: TestMetric): Data {
        return Data(preparedTestName,
                status.toStatus(),
                execution.startTime,
                execution.endTime,
                testMetric.expectedValue, testMetric.variance)
    }

    private fun getTestMetric(execution: TestResult): TestMetric {
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

    private fun parsePoolSummary(poolSummary: PoolSummary): List<Measure> {
        return poolSummary.tests
                .map { it.device }
                .distinct()
                .map { createMeasure(poolSummary.poolId, it) }
    }

    private fun createMeasure(pool: DevicePoolId, device: DeviceInfo): Measure {
        val data = parseData(pool, device)
        return Measure(device.serialNumber, calculateExecutionStats(data), data)
    }

    private fun parseList(pools: List<PoolSummary>): List<Measure> = pools.flatMap { this.parsePoolSummary(it) }

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

    fun parse(summary: Summary): ExecutionResult {
        logger.debug { summary }
        val failedTests = summary.pools.sumBy { it.failed }
        val passedTestCount = passedTestCount(summary)
        val measures = parseList(summary.pools)
        logger.debug { measures }
        val executionStats = aggregateExecutionStats(measures)
        logger.debug { executionStats }

        return ExecutionResult(passedTestCount, failedTests, executionStats, measures)
    }
}
