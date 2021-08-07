package com.malinskiy.marathon.vendor.junit4.executor.listener

import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.vendor.junit4.Junit4Device
import com.malinskiy.marathon.vendor.junit4.model.TestIdentifier

class DebugTestRunListener(private val device: Junit4Device, private val expectedTests: List<Test>) : JUnit4TestRunListener {

    private val logger = MarathonLogging.logger("DebugTestRunListener")

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        logger.info { "testRunStarted ${device.serialNumber}" }
    }

    override suspend fun testStarted(test: TestIdentifier) {
        logger.info { "testStarted ${device.serialNumber} test = $test" }
        if (!expectedTests.contains(test.toTest())) {
            logger.error {
                "Unexpected test ${
                    test.toTest().toTestName()
                }. Expected one of: ${expectedTests.joinToString(separator = "\n") { it.toTestName() }}"
            }
        }
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        logger.info { "testAssumptionFailure ${device.serialNumber} test = $test trace = $trace" }
    }

    override suspend fun testRunStopped(elapsedTime: Long) {
        logger.info { "testRunStopped ${device.serialNumber} elapsedTime = $elapsedTime" }
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        logger.info { "testFailed ${device.serialNumber} test = $test trace = $trace" }
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        logger.info { "testEnded ${device.serialNumber} test = $test" }
    }

    override suspend fun testIgnored(test: TestIdentifier) {
        logger.info { "testIgnored ${device.serialNumber} test = $test" }
    }

    override suspend fun testRunFailed(errorMessage: String) {
        logger.info { "testRunFailed ${device.serialNumber} errorMessage = $errorMessage" }
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        logger.info { "testRunEnded elapsedTime $elapsedTime" }
    }
}
