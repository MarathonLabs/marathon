package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device

sealed class DevicePoolMessage {
    data class AddDevice(val device: Device) : DevicePoolMessage()
    data class RemoveDevice(val device: Device) : DevicePoolMessage()
    object Terminate : DevicePoolMessage()
    sealed class MessageFromDevice(val device: Device, val sender: DeviceActor) : DevicePoolMessage() {
        class TestExecutionFinished(device: Device, sender: DeviceActor) : MessageFromDevice(device, sender)
        class Ready(device: Device, sender: DeviceActor) : MessageFromDevice(device, sender)
    }
}
