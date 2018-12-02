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
import kotlinx.coroutines.CompletableDeferred

class ProgressReportingListener(private val device: Device,
                                private val poolId: DevicePoolId,
                                private val testBatch: TestBatch,
                                private val deferredResults: CompletableDeferred<TestBatchResults>,
                                private val progressReporter: ProgressReporter,
                                private val testLogListener: TestLogListener): TestRunListener {

    private val success: MutableList<TestResult> = mutableListOf()
    private val failure: MutableList<TestResult> = mutableListOf()

    override fun batchFinished() {
        val received = (success + failure).map { it.test.toSafeTestName() }.toHashSet()

        val incompleteTests = testBatch.tests.filter {
            !received.contains(it.toSafeTestName())
        }

        deferredResults.complete(TestBatchResults(device, success, failure, incompleteTests))
    }

    override fun testFailed(test: Test, startTime: Long, endTime: Long) {
        progressReporter.testFailed(poolId, device, test)
        failure.add(TestResult(test, device.toDeviceInfo(), TestStatus.FAILURE, startTime, endTime, testLogListener.getLastLog()))
    }

    override fun testPassed(test: Test, startTime: Long, endTime: Long) {
        progressReporter.testPassed(poolId, device, test)
        success.add(TestResult(test, device.toDeviceInfo(), TestStatus.PASSED, startTime, endTime, testLogListener.getLastLog()))
    }

    override fun testStarted(test: Test) {
        progressReporter.testStarted(poolId, device, test)
    }
}
