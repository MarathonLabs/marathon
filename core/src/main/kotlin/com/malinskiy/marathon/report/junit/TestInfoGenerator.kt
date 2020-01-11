package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.execution.TestStatus
import java.text.SimpleDateFormat
import java.util.*

private val FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

internal class JunitReportGenerator(private val testEvents: List<TestEvent>) {

    @Suppress("MagicNumber")
    private fun Long.toJUnitSeconds(): String = (this / 1000.0).toString()

    var junitReports = hashMapOf<DevicePool, JUnitReport>()

    fun makeSuiteData() {
        val segregatedData = hashMapOf<DevicePool, List<TestEvent>>()
        testEvents.devicePools().forEach { deviceData ->
            segregatedData[deviceData] = testEvents.filter { it.device == deviceData.deviceInfo && it.poolId == deviceData.devicePool }
        }
        segregatedData.keys.forEach { deviceData ->
            junitReports[deviceData] = JUnitReport(
                segregatedData[deviceData]?.let { toTestSuite(it) }!!,
                segregatedData[deviceData]?.let { toTestCase(it) }!!
            )
        }
    }

    private fun toTestSuite(testEvents: List<TestEvent>) =
        TestSuiteData(
            name = "common",
            tests = testEvents.size,
            failures = testEvents.filter { it.testResult.status == TestStatus.FAILURE }.size,
            errors = 0,
            skipped = testEvents.filter { it.testResult.status == TestStatus.IGNORED || it.testResult.status == TestStatus.ASSUMPTION_FAILURE }.size,
            time = testEvents.map { it.testResult.durationMillis() }.sum().toJUnitSeconds(),
            timeStamp = FORMATTER.format(testEvents.maxBy { it.testResult.endTime }?.testResult?.endTime!!)
        )

    private fun toTestCase(testEvents: List<TestEvent>) =
        testEvents.map {
            TestCaseData(
                classname = it.testResult.test.pkg + "." + it.testResult.test.clazz,
                name = it.testResult.test.method,
                time = it.testResult.durationMillis().toJUnitSeconds(),
                skipped = if (it.testResult.status == TestStatus.IGNORED || it.testResult.status == TestStatus.ASSUMPTION_FAILURE) it.testResult.stacktrace else "",
                failure = if (it.testResult.status == TestStatus.FAILURE) it.testResult.stacktrace else ""
            )
        }
}

data class JUnitReport(
    val testSuiteData: TestSuiteData,
    val testCases: List<TestCaseData>
)

data class TestSuiteData(
    val name: String,
    val tests: Int,
    val failures: Int,
    val errors: Int,
    val skipped: Int,
    val time: String,
    val timeStamp: String
)

data class TestCaseData(
    val classname: String,
    val name: String,
    val time: String,
    val skipped: String?,
    val failure: String?
)

fun List<TestEvent>.devicePools() = this.map { DevicePool(it.poolId, it.device) }.distinct()
