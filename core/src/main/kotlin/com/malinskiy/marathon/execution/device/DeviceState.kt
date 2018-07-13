package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.test.TestBatch

sealed class DeviceState {
    object Connected : DeviceState()
    object Ready : DeviceState()
    object Initializing : DeviceState()
    data class Running(val testBatch: TestBatch) : DeviceState()
    object Terminated: DeviceState()
}
