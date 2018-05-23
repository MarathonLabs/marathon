package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.android.ddmlib.testrunner.TestRunResult
import com.malinskiy.marathon.android.toMarathonStatus
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.report.junit.JUnitReporter

class XmlListener(private val device: Device,
                  private val devicePoolId: DevicePoolId,
                  private val jUnitReporter: JUnitReporter) : NoOpTestRunListener() {

    private val runResult: TestRunResult = TestRunResult()

    override fun testRunStarted(runName: String, testCount: Int) {
        runResult.testRunStarted(runName, testCount)
    }

    override fun testStarted(test: TestIdentifier) {
        runResult.testStarted(test)
    }

    override fun testFailed(test: TestIdentifier, trace: String) {
        runResult.testFailed(test, trace)
    }

    override fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        runResult.testAssumptionFailure(test, trace)
    }

    override fun testIgnored(test: TestIdentifier) {
        runResult.testIgnored(test)
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        runResult.testEnded(test, testMetrics)
    }

    override fun testRunFailed(errorMessage: String) {
        runResult.testRunFailed(errorMessage)
    }

    override fun testRunStopped(elapsedTime: Long) {
        runResult.testRunStopped(elapsedTime)
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        runResult.testRunEnded(elapsedTime, runMetrics)
        generateReports()
    }

    private fun generateReports() {
        runResult.testResults.forEach {
            val status = it.value.status.toMarathonStatus()
            val testResult = TestResult(it.key!!.toTest(), status, it.value.startTime, it.value.endTime, it.value.stackTrace)
            jUnitReporter.testFinished(devicePoolId, device, testResult)
        }
    }
}
