package com.malinskiy.marathon.apple.listener

import com.malinskiy.marathon.apple.AppleDevice
import com.malinskiy.marathon.apple.listener.AppleTestRunListener
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test

class DebugTestRunListener(private val device: AppleDevice) : AppleTestRunListener {

    private val logger = MarathonLogging.logger("DebugTestRunListener")

    override suspend fun testRunStarted() {
        logger.info { "testRunStarted ${device.serialNumber}" }
    }

    override suspend fun testStarted(test: Test) {
        logger.info { "testStarted ${device.serialNumber} test = $test" }
    }
    
    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long, trace: String?) {
        logger.info { "testFailed ${device.serialNumber} test = $test, timespan = [$startTime,$endTime], trace = ${System.lineSeparator()}$trace" }
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        logger.info { "testPassed ${device.serialNumber} test = $test, timespan = [$startTime,$endTime]" }
    }

    override suspend fun testIgnored(test: Test, startTime: Long, endTime: Long) {
        logger.info { "testIgnored ${device.serialNumber} test = $test, timespan = [$startTime,$endTime]" }
    }

    override suspend fun testRunFailed(errorMessage: String, reason: DeviceFailureReason) {
        logger.info { "testRunFailed ${device.serialNumber} errorMessage = $errorMessage" }
    }

    override suspend fun testRunEnded() {
        logger.info { "testRunEnded" }
    }

    override suspend fun afterTestRun() {
        super.afterTestRun()
        logger.info { "afterTestRun" }
    }
}
