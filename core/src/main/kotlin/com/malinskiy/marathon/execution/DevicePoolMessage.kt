package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device

sealed class DevicePoolMessage {
    data class AddDevice(val device: Device) : DevicePoolMessage()
    data class RemoveDevice(val device: Device) : DevicePoolMessage()
    data class TestExecutionFinished(val device: Device, val sender: DeviceAktor) : DevicePoolMessage()
    data class Ready(val device: Device, val sender: DeviceAktor) : DevicePoolMessage()
    object Terminate : DevicePoolMessage()
}
