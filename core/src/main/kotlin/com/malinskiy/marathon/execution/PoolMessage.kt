package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test
import java.util.concurrent.Phaser

sealed class PoolMessage{
    class AddDevice(val device: Device) : PoolMessage()
    class RemoveDevice(val device: Device) : PoolMessage()
    class AddTest(val test : Test) : PoolMessage()
    object Initialize : PoolMessage()
    object Terminate : PoolMessage()
}