package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.TestBatch

sealed class DevicePoolMessage {
    sealed class FromScheduler : DevicePoolMessage() {
        data class AddDevice(val device: Device) : FromScheduler()
        data class RemoveDevice(val device: Device) : FromScheduler()
        object Terminate : FromScheduler()
    }

    sealed class FromDevice(val device: Device) : DevicePoolMessage() {
        class IsReady(device: Device) : FromDevice(device)
        class CompletedTestBatch(device: Device, val results: TestBatchResults) : FromDevice(device)
        class ReturnTestBatch(device: Device, val batch: TestBatch) : FromDevice(device)
    }

    sealed class FromQueue : DevicePoolMessage() {
        object Notify : FromQueue()
        object Terminated : FromQueue()
        data class ExecuteBatch(val device: Device, val batch: TestBatch) : FromQueue()
    }
}
