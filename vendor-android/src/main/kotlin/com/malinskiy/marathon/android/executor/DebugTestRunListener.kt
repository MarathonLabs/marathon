package com.malinskiy.marathon.android.executor

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier
import mu.KotlinLogging

class DebugTestRunListener(private val device: IDevice) : ITestRunListener {

    private val logger = KotlinLogging.logger("DebugTestRunListener")

    override fun testRunStarted(runName: String?, testCount: Int) {
        logger.warn { "testRunStarted ${device.serialNumber}" }
    }

    override fun testStarted(test: TestIdentifier?) {
        logger.warn { "testStarted ${device.serialNumber} test = $test" }
    }

    override fun testAssumptionFailure(test: TestIdentifier?, trace: String?) {
        logger.warn { "testAssumptionFailure ${device.serialNumber} test = $test trace = $trace" }
    }

    override fun testRunStopped(elapsedTime: Long) {
        logger.warn { "testRunStopped ${device.serialNumber} elapsedTime = $elapsedTime" }
    }

    override fun testFailed(test: TestIdentifier?, trace: String?) {
        logger.warn { "testFailed ${device.serialNumber} test = $test trace = $trace" }
    }

    override fun testEnded(test: TestIdentifier?, testMetrics: MutableMap<String, String>?) {
        logger.warn { "testEnded ${device.serialNumber} test = $test" }
    }

    override fun testIgnored(test: TestIdentifier?) {
        logger.warn { "testIgnored ${device.serialNumber} test = $test" }
    }

    override fun testRunFailed(errorMessage: String?) {
        logger.warn { "testRunFailed ${device.serialNumber} errorMessage = $errorMessage" }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>?) {
        logger.warn { "testRunEnded elapsedTime $elapsedTime" }
    }
}