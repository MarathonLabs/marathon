package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.Failed
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.Ready
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import mu.KotlinLogging
import kotlin.properties.Delegates

class DeviceActor(private val devicePoolId: DevicePoolId,
                  private val pool: SendChannel<DevicePoolMessage>,
                  private val configuration: Configuration,
                  private val device: Device,
                  private val analytics: Analytics,
                  private val retry: Channel<RetryMessage>,
                  private val progressReporter: ProgressReporter) : Actor<DeviceMessage>() {

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

    private val context = newSingleThreadContext(device.toString())

    private var currentBatch: TestBatch? = null

    private var job by Delegates.observable<Job?>(null) { _, _, newValue ->
        newValue?.invokeOnCompletion {
            if (it == null) {
                launch {
                    pool.send(Ready(device))
                }
                currentBatch = null
            } else {
                logger.error { it }
                launch {
                    pool.send(Failed(device))
                }
                terminate()
            }
        }
    }

    private fun initialize() {
        logger.debug { "initialize" }
        job = async(context) {
            device.prepare(configuration)
        }
    }

    private fun executeBatch(batch: TestBatch) {
        logger.debug { "executeBatch" }
        currentBatch = batch
        job = async(context) {
            device.execute(configuration, devicePoolId, batch, analytics, retry, progressReporter)
        }
    }

    private fun terminate() {
        logger.debug { "terminate" }
        job?.cancel()
        context.close()
        currentBatch?.let {
            launch {
                retry.send(RetryMessage.ReturnTestBatch(devicePoolId, it, device))
            }
        }
        close()
    }
}

sealed class DeviceMessage {
    data class Execute(val batch: TestBatch) : DeviceMessage()
    object Initialize : DeviceMessage()
    object Terminate : DeviceMessage()
    object WakeUp : DeviceMessage()
}
