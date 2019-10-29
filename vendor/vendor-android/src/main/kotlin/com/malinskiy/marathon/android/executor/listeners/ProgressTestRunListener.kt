package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.progress.ProgressReporter

class ProgressTestRunListener(
    private val device: Device,
    private val poolId: DevicePoolId,
    private val progressTracker: ProgressReporter
) : NoOpTestRunListener() {

    private val failed = mutableMapOf<TestIdentifier, Boolean>()
    private val ignored = mutableMapOf<TestIdentifier, Boolean>()

    override fun testStarted(test: TestIdentifier) {
        failed[test] = false
        ignored[test] = false
        progressTracker.testStarted(poolId, device.toDeviceInfo(), test.toTest())
    }

    override fun testFailed(test: TestIdentifier, trace: String) {
        failed[test] = true
    }

    override fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        testIgnored(test)
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        if (failed[test] == true) {
            progressTracker.testFailed(poolId, device.toDeviceInfo(), test.toTest())
        } else if (ignored[test] == false) {
            progressTracker.testPassed(poolId, device.toDeviceInfo(), test.toTest())
        }
    }

    override fun testIgnored(test: TestIdentifier) {
        ignored[test] = true
        progressTracker.testIgnored(poolId, device.toDeviceInfo(), test.toTest())
    }
}
