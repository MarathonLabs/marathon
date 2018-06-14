package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device

sealed class DevicePoolMessage {
    sealed class FromScheduler : DevicePoolMessage() {
        data class AddDevice(val device: Device) : FromScheduler()
        data class RemoveDevice(val device: Device) : FromScheduler()
        object Terminate : FromScheduler()
    }

    sealed class FromDevice(val device: Device, val sender: DeviceActor) : DevicePoolMessage() {
        class RequestNextBatch(device: Device, sender: DeviceActor) : FromDevice(device, sender)
    }

    sealed class FromQueue : DevicePoolMessage(){
        object Notify : FromQueue()
    }
}
