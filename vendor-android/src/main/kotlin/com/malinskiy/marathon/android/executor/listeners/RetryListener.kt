package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.android.ddmlib.testrunner.TestResult
import com.android.ddmlib.testrunner.TestRunResult
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestFailed
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch

class RetryListener(private val testBatch: TestBatch,
                    private val device: Device,
                    private val retryChannel: SendChannel<TestFailed>,
                    private val devicePoolId: DevicePoolId) : AbstractTestRunResultListener() {
    override fun handleTestRunResults(runResult: TestRunResult) {
        val results = runResult.testResults
        val successful = results.all { it.value.isSuccessful() }
        if (!successful || testBatch.tests.size != runResult.testResults.size) {
            val tests = testBatch.tests.associateBy { it.identifier() }

            val failed = tests.filterNot { results[it.key]?.isSuccessful() ?: false }.values

            launch {
                retryChannel.send(TestFailed(devicePoolId, failed, device))
            }
        }
    }

    private fun Test.identifier(): TestIdentifier {
        return TestIdentifier("$pkg.$clazz", method)
    }

    private fun TestResult.isSuccessful() = status == TestResult.TestStatus.PASSED
            || status == TestResult.TestStatus.IGNORED

}
