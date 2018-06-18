package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.android.ddmlib.testrunner.TestRunResult
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.android.toMarathonStatus
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestResult
import com.android.ddmlib.testrunner.TestResult as DdmLibTestResult

class AnalyticsListener(private val device: Device,
                        private val devicePoolId: DevicePoolId,
                        private val analytics: Analytics) : NoOpTestRunResultListener() {
    override fun handleTestRunResults(runResult: TestRunResult) {
        runResult.testResults.forEach {
            analytics.trackTestResult(devicePoolId, device, it.toTestResult(device))
        }
    }
}

fun Map.Entry<TestIdentifier, DdmLibTestResult>.toTestResult(device: Device): TestResult {
    return TestResult(test = key.toTest(),
            device = device.toDeviceInfo(),
            status = value.status.toMarathonStatus(),
            startTime = value.startTime,
            endTime = value.endTime,
            stacktrace = value.stackTrace)
}
