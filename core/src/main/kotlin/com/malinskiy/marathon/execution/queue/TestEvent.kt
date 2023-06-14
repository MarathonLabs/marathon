package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestResult

sealed class TestEvent {
    object Started : TestEvent()
    
    data class Failed(
        val device: DeviceInfo,
        val testResult: TestResult
    ) : TestEvent()

    data class Passed(
        val device: DeviceInfo,
        val testResult: TestResult
    ) : TestEvent()
    
    data class RemoveAttempts(val count: Int) : TestEvent()

    object AddRetry : TestEvent()

    data class Incomplete(
        val device: DeviceInfo,
        val testResult: TestResult,
        val final: Boolean
    ) : TestEvent()
}
