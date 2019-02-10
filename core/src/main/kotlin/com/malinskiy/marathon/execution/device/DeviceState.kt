package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred

sealed class DeviceState {
    object Connected : DeviceState()
    object Ready : DeviceState()
    object Initializing : DeviceState()
    data class Running(val testBatch: TestBatch,
                       val result: CompletableDeferred<TestBatchResults>) : DeviceState()
    object Terminated : DeviceState()

    override fun toString(): String = "DeviceState.${this::class.java.simpleName}"
}
