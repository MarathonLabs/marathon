package com.malinskiy.marathon.report

import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test

class IgnoredTestsReporter(val analytics: Analytics) {
    val fakeDevicePoolId = DevicePoolId("ignored")

    private val fakeDevice = DeviceInfo(operatingSystem = OperatingSystem("Fake OS"),
            serialNumber = "virtual",
            model = "virtual",
            manufacturer = "virtual",
            networkState = NetworkState.CONNECTED,
            deviceFeatures = emptyList(),
            healthy = true)

    init {
        analytics.trackDeviceConnected(fakeDevicePoolId, fakeDevice)
    }

    fun reportTest(test: Test) {
        val tr = TestResult(test = test,
                device = fakeDevice,
                status = TestStatus.IGNORED,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis())

        analytics.trackRawTestRun(fakeDevicePoolId, fakeDevice, tr)
        analytics.trackTestFinished(fakeDevicePoolId, fakeDevice, tr)
    }
}