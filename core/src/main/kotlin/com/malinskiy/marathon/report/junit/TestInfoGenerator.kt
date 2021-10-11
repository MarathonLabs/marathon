package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.report.junit.model.JUnitReport
import com.malinskiy.marathon.report.junit.model.Pool
import com.malinskiy.marathon.report.junit.model.StackTraceElement
import com.malinskiy.marathon.report.junit.model.TestCaseData
import com.malinskiy.marathon.report.junit.model.TestSuiteData
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

internal class JunitReportGenerator(private val testEvents: List<TestEvent>) {

    @Suppress("MagicNumber")
    private fun Long.toJUnitSeconds(): String = (this / 1000.0).toString()

    var junitReports = hashMapOf<Pool, JUnitReport>()

    fun makeSuiteData() {
        val segregatedData = hashMapOf<Pool, List<TestEvent>>()
        testEvents.map { Pool(it.poolId, it.device) }.distinct()
            .forEach { deviceData ->
            segregatedData[deviceData] = testEvents.filter { it.device == deviceData.deviceInfo && it.poolId == deviceData.devicePoolId }
        }
        segregatedData.keys.forEach { deviceData ->
            val testEvents = segregatedData[deviceData]
            if (testEvents != null) {
                junitReports[deviceData] = JUnitReport(toTestSuite(testEvents), toTestCase(testEvents))
            }
        }
    }

    private fun toTestSuite(testEvents: List<TestEvent>) =
        TestSuiteData(
            name = "common",
            tests = testEvents.size,
            failures = testEvents.filter { isFailure(it.testResult) }.size,
            errors = 0,
            skipped = testEvents.filter { isSkipped(it.testResult) }.size,
            time = testEvents.sumOf { it.testResult.durationMillis() }.toJUnitSeconds(),
            timeStamp = FORMATTER.format(testEvents.maxByOrNull { it.testResult.endTime }?.testResult?.endTime)
        )

    private fun toTestCase(testEvents: List<TestEvent>) =
        testEvents.map {
            TestCaseData(
                classname = it.testResult.test.pkg + "." + it.testResult.test.clazz,
                name = it.testResult.test.method,
                time = it.testResult.durationMillis().toJUnitSeconds(),
                skipped = if (isSkipped(it.testResult)) StackTraceElement(true, it.testResult.stacktrace) else StackTraceElement(false, null),
                failure = if (isFailure(it.testResult)) StackTraceElement(true, it.testResult.stacktrace) else StackTraceElement(false, null)
            )
        }

    private fun isSkipped(testResult: TestResult) =
        testResult.status == TestStatus.IGNORED || testResult.status == TestStatus.ASSUMPTION_FAILURE

    private fun isFailure(testResult: TestResult) =
        testResult.status == TestStatus.FAILURE || testResult.status == TestStatus.INCOMPLETE
}
