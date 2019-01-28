package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestResult

sealed class TestEvent {
    data class Failed(val device: DeviceInfo,
                      val testResult: TestResult) : TestEvent()

    data class Passed(val device: DeviceInfo,
                      val testResult: TestResult) : TestEvent()

    data class Remove(val diff: Int) : TestEvent()

    data class Retry(val device: DeviceInfo,
                     val testResult: TestResult) : TestEvent()
}
