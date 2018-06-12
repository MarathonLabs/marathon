package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.test.TestBatch

class DeviceActor(private val devicePoolId: DevicePoolId,
                  private val pool: Actor<DevicePoolMessage>,
                  private val configuration: Configuration,
                  private val device: Device,
                  private val analytics: Analytics) : Actor<DeviceMessage>() {

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
        device.execute(configuration, devicePoolId, batch, analytics)
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
