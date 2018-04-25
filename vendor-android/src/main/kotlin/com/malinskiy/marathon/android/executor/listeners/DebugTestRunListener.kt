package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier
import mu.KotlinLogging

class DebugTestRunListener(private val device: IDevice) : ITestRunListener {

    private val logger = KotlinLogging.logger("DebugTestRunListener")

    override fun testRunStarted(runName: String?, testCount: Int) {
        logger.info { "testRunStarted ${device.serialNumber}" }
    }

    override fun testStarted(test: TestIdentifier?) {
        logger.info { "testStarted ${device.serialNumber} test = $test" }
    }

    override fun testAssumptionFailure(test: TestIdentifier?, trace: String?) {
        logger.info { "testAssumptionFailure ${device.serialNumber} test = $test trace = $trace" }
    }

    override fun testRunStopped(elapsedTime: Long) {
        logger.info { "testRunStopped ${device.serialNumber} elapsedTime = $elapsedTime" }
    }

    override fun testFailed(test: TestIdentifier?, trace: String?) {
        logger.info { "testFailed ${device.serialNumber} test = $test trace = $trace" }
    }

    override fun testEnded(test: TestIdentifier?, testMetrics: MutableMap<String, String>?) {
        logger.info { "testEnded ${device.serialNumber} test = $test" }
    }

    override fun testIgnored(test: TestIdentifier?) {
        logger.info { "testIgnored ${device.serialNumber} test = $test" }
    }

    override fun testRunFailed(errorMessage: String?) {
        logger.info { "testRunFailed ${device.serialNumber} errorMessage = $errorMessage" }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>?) {
        logger.info { "testRunEnded elapsedTime $elapsedTime" }
    }
}