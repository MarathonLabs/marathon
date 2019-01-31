package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestResult

sealed class TestAction {
    data class SaveReport(val deviceInfo: DeviceInfo, val testResult: TestResult) : TestAction()
}
