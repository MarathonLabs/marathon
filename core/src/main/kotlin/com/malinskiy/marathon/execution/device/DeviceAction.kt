package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.test.TestBatch

sealed class DeviceAction {
    object Initialize : DeviceAction()
    data class Terminate(val batch: TestBatch? = null) : DeviceAction()
    data class ExecuteBatch(val batch: TestBatch) : DeviceAction()
    object RequestNextBatch : DeviceAction()
}