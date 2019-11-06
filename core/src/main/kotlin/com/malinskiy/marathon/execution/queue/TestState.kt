package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestResult

sealed class TestState {
    data class Added(val count: Int) : TestState()

    data class Executed(
        val device: DeviceInfo,
        val testResult: TestResult,
        val count: Int
    ) : TestState()

    data class Failed(
        val device: DeviceInfo,
        val testResult: TestResult
    ) : TestState()

    data class Passed(
        val device: DeviceInfo,
        val testResult: TestResult
    ) : TestState()
}
