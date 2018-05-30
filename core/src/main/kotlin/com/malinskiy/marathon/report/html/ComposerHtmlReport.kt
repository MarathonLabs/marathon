package com.malinskiy.marathon.report.html

import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.report.*


class ComposerHtmlReport(private val configuration: Configuration) : HtmlReportPrinter {
    override fun print(testReport: Summary) {
        writeHtmlReport(Gson(), testReport, configuration.outputDir)
    }
}

data class Summary(val pools: List<PoolSummary>)

data class PoolSummary(val poolId: DevicePoolId,
                       val tests: List<TestResult>,
                       val passed: Int,
                       val ignored: Int,
                       val failed: Int,
                       val durationMillis: Long,
                       val devices: List<Device>)

fun Summary.toHtmlIndex() = HtmlIndex(suites = pools.map { it.toHtmlSuite() })

fun PoolSummary.toHtmlSuite() = HtmlSuite(
        id = poolId.name,
        tests = tests.map { it.toShortHtmlTest() },
        passedCount = passed,
        ignoredCount = ignored,
        failedCount = failed,
        durationMillis = durationMillis,
        devices = devices.map { it.toHtmlDevice() }
)

fun TestResult.toShortHtmlTest() = HtmlShortTest(
        id = "${test.pkg}.${test.clazz}#${test.method}",
        packageName = test.pkg,
        className = test.clazz,
        name = test.method,
        durationMillis = endTime - startTime,
        status = status.toHtmlStatus(),
        deviceId = device.serialNumber,
        deviceModel = device.model,
        properties = emptyMap() //TODO: provide real properties map
)

fun TestResult.toHtmlFullTest(poolId: DevicePoolId) = HtmlFullTest(
        suiteId = poolId.name,
        id = "${test.pkg}.${test.clazz}#${test.method}",
        packageName = test.pkg,
        className = test.clazz,
        name = test.method,
        durationMillis = endTime - startTime,
        status = status.toHtmlStatus(),
        deviceId = device.serialNumber,
        deviceModel = device.model,
        stacktrace = stacktrace,
        logcatPath = "",
        filePaths = emptyList(),
        screenshots = emptyList(),
        properties = emptyMap() //TODO: provide real properties map
)

fun Device.toHtmlDevice() = HtmlDevice(
        id = serialNumber,
        model = model,
        logcatPath = "",
        instrumentationOutputPath = ""
)

fun TestStatus.toHtmlStatus() = when (this) {
    TestStatus.PASSED -> HtmlStatus.Passed
    TestStatus.FAILURE -> HtmlStatus.Failed
    TestStatus.IGNORED -> HtmlStatus.Ignored
    else -> HtmlStatus.Failed
}