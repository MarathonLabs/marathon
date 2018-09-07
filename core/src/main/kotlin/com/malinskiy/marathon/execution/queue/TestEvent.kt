package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.execution.TestResult

sealed class TestEvent {
    data class Failed(val device: Device,
                      val testResult: TestResult) : TestEvent()

    data class Passed(val device: Device,
                      val testResult: TestResult) : TestEvent()

    data class Remove(val diff: Int) : TestEvent()

    data class Retry(val device: Device,
                     val testResult: TestResult) : TestEvent()
}
