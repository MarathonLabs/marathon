package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test
import java.util.concurrent.Phaser

sealed class PoolMessage {
    data class AddDevice(val device: Device) : PoolMessage()
    data class RemoveDevice(val device: Device) : PoolMessage()
    data class TestExecutionFinished(val device: Device, val sender: DeviceAktor) : PoolMessage()
    data class Ready(val device: Device, val sender: DeviceAktor) : PoolMessage()
    object Terminate : PoolMessage()
}