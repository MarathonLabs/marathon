package com.malinskiy.marathon.execution

import com.malinskiy.marathon.aktor.Aktor
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.test.TestBatch

class DeviceAktor(private val devicePoolId: DevicePoolId,
                  private val pool: Aktor<DevicePoolMessage>,
                  private val configuration: Configuration,
                  private val device: Device) : Aktor<DeviceMessage>() {

    override suspend fun receive(msg: DeviceMessage) {
        when (msg) {
            is DeviceMessage.Initialize -> initialize()
            is DeviceMessage.ExecuteTestBatch -> executeBatch(msg.batch)
            is DeviceMessage.Terminate -> terminate()
        }
    }

    private suspend fun initialize() {
        device.prepare(configuration)
        pool.send(DevicePoolMessage.Ready(device, this))
    }

    private suspend fun executeBatch(batch: TestBatch) {
        device.execute(configuration, devicePoolId, batch)
        pool.send(DevicePoolMessage.TestExecutionFinished(device, this))
    }

    private fun terminate() {
        close()
    }
}

sealed class DeviceMessage {
    data class ExecuteTestBatch(val batch: TestBatch) : DeviceMessage()
    object Initialize : DeviceMessage()
    object Terminate : DeviceMessage()
}
