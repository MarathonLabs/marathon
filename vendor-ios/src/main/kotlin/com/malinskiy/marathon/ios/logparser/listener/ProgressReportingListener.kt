package com.malinskiy.marathon.ios.logparser.listener

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter
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

    override fun batchFinished() {
        val received = (success + failure).map { it.test.toSafeTestName() }.toHashSet()

        val incompleteTests = testBatch.tests.filter {
            !received.contains(it.toSafeTestName())
        }

        deferred.complete(TestBatchResults(device, success, failure, incompleteTests))
    }

    override fun testFailed(test: Test, startTime: Long, endTime: Long) {
        progressTracker.testFailed(poolId, device, test)
        failure.add(TestResult(test, device.toDeviceInfo(), TestStatus.FAILURE, startTime, endTime, testLogListener.getLastLog()))
    }

    override fun testPassed(test: Test, startTime: Long, endTime: Long) {
        progressTracker.testPassed(poolId, device, test)
        success.add(TestResult(test, device.toDeviceInfo(), TestStatus.PASSED, startTime, endTime, testLogListener.getLastLog()))
    }

    override fun testStarted(test: Test) {
        progressTracker.testStarted(poolId, device, test)
    }
}
