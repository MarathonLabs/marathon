package com.malinskiy.marathon.android.executor

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier
import mu.KotlinLogging

class DebugTestRunListener(private val device: IDevice) : ITestRunListener {

    private val logger = KotlinLogging.logger("DebugTestRunListener")

    override fun testRunStarted(runName: String?, testCount: Int) {
        logger.debug { "testRunStarted ${device.serialNumber}" }
    }

    override fun testStarted(test: TestIdentifier?) {
        logger.debug { "testStarted ${device.serialNumber} test = $test" }
    }

    override fun testAssumptionFailure(test: TestIdentifier?, trace: String?) {
        logger.debug { "testAssumptionFailure ${device.serialNumber} test = $test trace = $trace" }
    }

    override fun testRunStopped(elapsedTime: Long) {
        logger.debug { "testRunStopped ${device.serialNumber} elapsedTime = $elapsedTime" }
    }

    override fun testFailed(test: TestIdentifier?, trace: String?) {
        logger.debug { "testFailed ${device.serialNumber} test = $test trace = $trace" }
    }

    override fun testEnded(test: TestIdentifier?, testMetrics: MutableMap<String, String>?) {
        logger.debug { "testEnded ${device.serialNumber} test = $test" }
    }

    override fun testIgnored(test: TestIdentifier?) {
        logger.debug { "testIgnored ${device.serialNumber} test = $test" }
    }

    override fun testRunFailed(errorMessage: String?) {
        logger.debug { "testRunFailed ${device.serialNumber} errorMessage = $errorMessage" }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>?) {
        logger.debug { "testRunEnded elapsedTime $elapsedTime" }
    }
}