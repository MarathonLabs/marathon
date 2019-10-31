package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.Test

class ProgressTestRunListener(
    private val device: Device,
    private val poolId: DevicePoolId,
    private val progressTracker: ProgressReporter
) : TestRunListener {

    private val failed = mutableMapOf<Test, Boolean>()
    private val ignored = mutableMapOf<Test, Boolean>()

    override fun testStarted(test: Test) {
        failed[test] = false
        ignored[test] = false
        progressTracker.testStarted(poolId, device.toDeviceInfo(), test)
    }

    override fun testFailed(test: Test, trace: String) {
        failed[test] = true
    }

    override fun testAssumptionFailure(test: Test, trace: String) {
        testIgnored(test)
    }

    override fun testEnded(test: Test, testMetrics: Map<String, String>) {
        if (failed[test] == true) {
            progressTracker.testFailed(poolId, device.toDeviceInfo(), test)
        } else if (ignored[test] == false) {
            progressTracker.testPassed(poolId, device.toDeviceInfo(), test)
        }
    }

    override fun testIgnored(test: Test) {
        ignored[test] = true
        progressTracker.testIgnored(poolId, device.toDeviceInfo(), test)
    }
}
