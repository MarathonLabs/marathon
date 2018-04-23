package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test
import java.util.concurrent.Phaser

sealed class PoolMessage{
    class AddDevice(val device: Device, val complete: Phaser) : PoolMessage()
    class RemoveDevice(val device: Device, val complete: Phaser) : PoolMessage()
    class AddTest(val test : Test) : PoolMessage()
    object Terminate : PoolMessage()
}