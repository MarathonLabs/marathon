package com.malinskiy.marathon.ios.logparser.listener

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toSafeTestName
import kotlinx.coroutines.experimental.CompletableDeferred

class ProgressReportingListener(private val device: Device,
                                private val poolId: DevicePoolId,
                                private val progressTracker: ProgressReporter,
                                private val deferred: CompletableDeferred<TestBatchResults>,
                                private val testBatch: TestBatch,
                                private val testLogListener: TestLogListener): TestRunListener {

    val success: MutableList<TestResult> = mutableListOf()
    val failure: MutableList<TestResult> = mutableListOf()

    val logger = MarathonLogging.logger(javaClass.simpleName)

    override fun batchFinished() {
        val received = (success + failure).map { it.test.toSafeTestName() }.toHashSet()

        logger.debug { "Batch: " + testBatch.tests.map { it.toSafeTestName() }.joinToString(", ") }
        logger.debug { "  success: " + success.map { it.test.toSafeTestName() }.joinToString(", ") }
        logger.debug { "  failure: " + failure.map { it.test.toSafeTestName() }.joinToString(", ") }

        val incompleteTests = testBatch.tests.filter {
            !received.contains(it.toSafeTestName())
        }

        val incomplete = incompleteTests.map {
            TestResult(it, device.toDeviceInfo(), TestStatus.INCOMPLETE, 0, 0, null)
        }

        deferred.complete(TestBatchResults(device, success, failure + incomplete))
    }

    override fun testFailed(test: Test, startTime: Long, endTime: Long) {
        progressTracker.testFailed(poolId, device, test)
        failure.add(TestResult(test, device.toDeviceInfo(), TestStatus.FAILURE, startTime, endTime, testLogListener.getLastLog()))
        logger.debug { "Test failed " + test.toSafeTestName() }
    }

    override fun testPassed(test: Test, startTime: Long, endTime: Long) {
        progressTracker.testPassed(poolId, device, test)
        success.add(TestResult(test, device.toDeviceInfo(), TestStatus.PASSED, startTime, endTime, testLogListener.getLastLog()))
        logger.debug { "Test passed " + test.toSafeTestName() }
        }

    override fun testStarted(test: Test) {
        progressTracker.testStarted(poolId, device, test)
        logger.debug { "Test started " + test.toSafeTestName() }
    }
}
