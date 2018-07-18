package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.execution.TestResult

sealed class TestAction {
    data class SaveReport(val device: Device, val testResult: TestResult) : TestAction()
}
