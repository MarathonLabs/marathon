package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.test.TestBatch

sealed class DevicePoolMessage {
    sealed class FromScheduler : DevicePoolMessage() {
        data class AddDevice(val device: Device) : FromScheduler()
        data class RemoveDevice(val device: Device) : FromScheduler()
        object Terminate : FromScheduler()
    }

    sealed class FromDevice(open val device: Device) : DevicePoolMessage() {
        data class IsReady(override val device: Device) : FromDevice(device)
        data class CompletedTestBatch(override val device: Device, val results: TestBatchResults) : FromDevice(device)
        data class ReturnTestBatch(override val device: Device, val batch: TestBatch, val reason: String) : FromDevice(device)
    }

    sealed class FromQueue : DevicePoolMessage() {
        object Notify : FromQueue()
        object Terminated : FromQueue()
        data class ExecuteBatch(val device: DeviceInfo, val batch: TestBatch) : FromQueue()
    }
}
