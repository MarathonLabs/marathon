package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestRunResult
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.android.toMarathonStatus
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestResult
import com.android.ddmlib.testrunner.TestResult as DdmLibTestResult
import com.android.ddmlib.testrunner.TestResult.TestStatus as DdmLibTestStatus

class AnalyticsListener(private val device: Device,
                        private val devicePoolId: DevicePoolId,
                        private val analytics: Analytics) : NoOpTestRunResultListener() {
    override fun handleTestRunResults(runResult: TestRunResult) {
        runResult.testResults.forEach {
            val status = it.value.status.toMarathonStatus()
            val testResult = TestResult(it.key!!.toTest(), device.toDeviceInfo(), status, it.value.startTime, it.value.endTime, it.value.stackTrace)

            analytics.trackTestResult(devicePoolId, device, testResult)
        }
    }
}
