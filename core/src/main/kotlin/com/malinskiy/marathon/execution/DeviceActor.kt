package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.Ready
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.Failed
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import mu.KotlinLogging

class DeviceActor(private val devicePoolId: DevicePoolId,
                  private val pool: SendChannel<DevicePoolMessage>,
                  private val configuration: Configuration,
                  private val device: Device,
                  private val analytics: Analytics,
                  private val retry: Channel<TestFailed>) : Actor<DeviceMessage>() {

    private val logger = KotlinLogging.logger("DevicePool[${devicePoolId.name}]_DeviceActor[${device.serialNumber}]")

    override suspend fun receive(msg: DeviceMessage) {
        when (msg) {
            is DeviceMessage.Initialize -> initialize()
            is DeviceMessage.Execute -> executeBatch(msg.batch)
            is DeviceMessage.Terminate -> terminate()
            is DeviceMessage.WakeUp -> wakeUp()
        }
    }

    private suspend fun wakeUp() {
        logger.debug { "WakeUp" }
        pool.send(Ready(device))
    }

    private suspend fun initialize() {
        logger.debug { "initialize" }
        try {
            device.prepare(configuration)
            pool.send(Ready(device))
        } catch (th: Throwable) {
            logger.error { th }
            pool.send(Failed(device))
            terminate()
        }
    }

    private suspend fun executeBatch(batch: TestBatch) {
        logger.debug { "executeBatch" }
        try {
            device.execute(configuration, devicePoolId, batch, analytics, retry)
            pool.send(Ready(device))
        } catch (th: Throwable) {
            logger.error { th }
            pool.send(Failed(device))
            terminate()
        }
    }

    private fun terminate() {
        logger.debug { "terminate" }
        close()
    }
}

sealed class DeviceMessage {
    data class Execute(val batch: TestBatch) : DeviceMessage()
    object Initialize : DeviceMessage()
    object Terminate : DeviceMessage()
    object WakeUp : DeviceMessage()
}
