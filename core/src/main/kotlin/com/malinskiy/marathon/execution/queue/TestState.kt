package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestResult

sealed class TestState {
    data class Added(val count: Int) : TestState()

    data class Executing(val count: Int,
                         val testRuns: List<Pair<DeviceInfo, TestResult>>) : TestState()

    data class Executed(val testRuns: List<Pair<DeviceInfo, TestResult>>) : TestState()

}
