package com.malinskiy.marathon.analytics.tracker.local

import com.malinskiy.marathon.analytics.tracker.NoOpTracker
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.report.internal.TestResultReporter

internal class TestResultsTracker(private val testResultReporter: TestResultReporter) : NoOpTracker() {

    override fun trackTestFinished(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        testResultReporter.testFinished(poolId, device, testResult)
    }
}
