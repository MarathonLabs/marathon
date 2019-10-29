package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test

class DebugTestRunListener(private val device: AndroidDevice) : AndroidTestRunListener {

    private val logger = MarathonLogging.logger("DebugTestRunListener")

    override fun testRunStarted(runName: String, testCount: Int) {
        logger.info { "testRunStarted ${device.serialNumber}" }
    }

    override fun testStarted(test: Test) {
        logger.info { "testStarted ${device.serialNumber} test = $test" }
    }

    override fun testAssumptionFailure(test: Test, trace: String) {
        logger.info { "testAssumptionFailure ${device.serialNumber} test = $test trace = $trace" }
    }

    override fun testRunStopped(elapsedTime: Long) {
        logger.info { "testRunStopped ${device.serialNumber} elapsedTime = $elapsedTime" }
    }

    override fun testFailed(test: Test, trace: String) {
        logger.info { "testFailed ${device.serialNumber} test = $test trace = $trace" }
    }

    override fun testEnded(test: Test, testMetrics: Map<String, String>) {
        logger.info { "testEnded ${device.serialNumber} test = $test" }
    }

    override fun testIgnored(test: Test) {
        logger.info { "testIgnored ${device.serialNumber} test = $test" }
    }

    override fun testRunFailed(errorMessage: String) {
        logger.info { "testRunFailed ${device.serialNumber} errorMessage = $errorMessage" }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        logger.info { "testRunEnded elapsedTime $elapsedTime" }
    }
}
