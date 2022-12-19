package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.ios.AppleDevice
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test

class DebugTestRunListener(private val device: AppleDevice) : AppleTestRunListener {

    private val logger = MarathonLogging.logger("DebugTestRunListener")

    override suspend fun testRunStarted() {
        logger.debug { "testRunStarted ${device.serialNumber}" }
    }

    override suspend fun testStarted(test: Test) {
        logger.debug { "testStarted ${device.serialNumber} test = $test" }
    }
    
    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long) {
        logger.debug { "testFailed ${device.serialNumber} test = $test, timespan = [$startTime,$endTime]" }
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        logger.debug { "testPassed ${device.serialNumber} test = $test, timespan = [$startTime,$endTime]" }
    }

    override suspend fun testRunFailed(errorMessage: String, reason: DeviceFailureReason) {
        logger.debug { "testRunFailed ${device.serialNumber} errorMessage = $errorMessage" }
    }

    override suspend fun testRunEnded() {
        logger.debug { "testRunEnded" }
    }
}
