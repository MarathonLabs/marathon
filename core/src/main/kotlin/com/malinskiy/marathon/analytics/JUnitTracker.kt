package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.report.junit.JUnitReporter
import com.malinskiy.marathon.test.Test

class JUnitTracker(private val jUnitReporter: JUnitReporter) : NoOpTracker() {
    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        jUnitReporter.testFinished(poolId, device, testResult)
    }
}
