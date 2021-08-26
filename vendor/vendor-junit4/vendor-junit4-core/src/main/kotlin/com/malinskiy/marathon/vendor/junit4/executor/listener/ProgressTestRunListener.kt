package com.malinskiy.marathon.vendor.junit4.executor.listener

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.vendor.junit4.model.TestIdentifier

class ProgressTestRunListener(
    private val device: Device,
    private val poolId: DevicePoolId,
    private val progressTracker: ProgressReporter
) : NoOpTestRunListener() {

    private val failed = mutableMapOf<TestIdentifier, Boolean>()
    private val ignored = mutableMapOf<TestIdentifier, Boolean>()

    override suspend fun testStarted(test: TestIdentifier) {
        failed[test] = false
        ignored[test] = false
        progressTracker.testStarted(poolId, device.toDeviceInfo(), test.toTest())
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        failed[test] = true
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        testIgnored(test)
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        if (failed[test] == true) {
            progressTracker.testFailed(poolId, device.toDeviceInfo(), test.toTest())
        } else if (ignored[test] == false) {
            progressTracker.testPassed(poolId, device.toDeviceInfo(), test.toTest())
        }
    }

    override suspend fun testIgnored(test: TestIdentifier) {
        ignored[test] = true
        progressTracker.testIgnored(poolId, device.toDeviceInfo(), test.toTest())
    }
}
