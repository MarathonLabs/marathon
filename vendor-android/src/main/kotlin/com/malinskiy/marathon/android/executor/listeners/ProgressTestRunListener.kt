package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.progress.ProgressReporter

class ProgressTestRunListener(private val device: Device,
                              private val poolId: DevicePoolId,
                              private val progressTracker: ProgressReporter) : NoOpTestRunListener() {

    override fun testStarted(test: TestIdentifier) {
        progressTracker.testStarted(poolId, device, test.toTest())
    }

    override fun testFailed(test: TestIdentifier, trace: String) {
        progressTracker.testFailed(poolId, device, test.toTest())
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        progressTracker.testEnded(poolId, device, test.toTest())
    }

    override fun testIgnored(test: TestIdentifier) {
        progressTracker.testIgnored(poolId, device, test.toTest())
    }
}
