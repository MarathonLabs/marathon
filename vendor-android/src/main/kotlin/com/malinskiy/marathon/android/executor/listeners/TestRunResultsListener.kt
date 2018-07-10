package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.android.ddmlib.testrunner.TestResult
import com.android.ddmlib.testrunner.TestRunResult as DdmLibTestRunResult
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.QueueMessage
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch

class TestRunResultsListener(private val testBatch: TestBatch,
                             private val device: Device,
                             private val retryChannel: SendChannel<QueueMessage.RetryMessage>,
                             private val devicePoolId: DevicePoolId) : AbstractTestRunResultListener() {
    override fun handleTestRunResults(runResult: DdmLibTestRunResult) {
        val results = runResult.testResults
        val tests = testBatch.tests.associateBy { it.identifier() }

        val finished = tests.filter {
            results[it.key]?.isSuccessful() ?: false
        }.values

        val failed = tests.filterNot {
            results[it.key]?.isSuccessful() ?: false
        }.values

        launch {
            retryChannel.send(QueueMessage.RetryMessage.TestRunResults(devicePoolId, finished, failed, device))
        }
    }


    private fun Test.identifier(): TestIdentifier {
        return TestIdentifier("$pkg.$clazz", method)
    }

    private fun TestResult.isSuccessful() =
            status == TestResult.TestStatus.PASSED || status == TestResult.TestStatus.IGNORED

}
