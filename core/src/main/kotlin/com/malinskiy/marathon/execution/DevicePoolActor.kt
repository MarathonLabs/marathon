package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.actor.safeSend
import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.device.DeviceActor
import com.malinskiy.marathon.execution.device.DeviceEvent
import com.malinskiy.marathon.execution.progress.PoolProgressAccumulator
import com.malinskiy.marathon.execution.queue.QueueActor
import com.malinskiy.marathon.execution.queue.QueueMessage
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext

class DevicePoolActor(
    private val poolId: DevicePoolId,
    private val configuration: Configuration,
    private val poolProgressAccumulator: PoolProgressAccumulator,
    analytics: Analytics,
    shard: TestShard,
    timer: Timer,
    parent: Job,
    context: CoroutineContext,
    testBundleIdentifier: TestBundleIdentifier?,
) :
    Actor<DevicePoolMessage>(parent = parent, context = context) {

    private val logger = MarathonLogging.logger("DevicePoolActor[${poolId.name}]")

    override suspend fun receive(msg: DevicePoolMessage) {
        when (msg) {
            is DevicePoolMessage.FromScheduler.AddDevice -> addDevice(msg.device)
            is DevicePoolMessage.FromScheduler.RemoveDevice -> removeDevice(msg.device)
            is DevicePoolMessage.FromScheduler.Terminate -> terminate()
            is DevicePoolMessage.FromDevice.IsReady -> deviceReady(msg)
            is DevicePoolMessage.FromDevice.CompletedTestBatch -> deviceCompleted(msg.device, msg.results)
            is DevicePoolMessage.FromDevice.ReturnTestBatch -> deviceReturnedTestBatch(msg.device, msg.batch, msg.reason)
            is DevicePoolMessage.FromQueue.Notify -> notifyDevices()
            is DevicePoolMessage.FromQueue.Terminated -> onQueueTerminated()
            is DevicePoolMessage.FromQueue.ExecuteBatch -> executeBatch(msg.device, msg.batch)
        }
    }

    /**
     * Any problem with a device should not propagate a cancellation upstream
     */
    private val poolJob = SupervisorJob(parent)

    private val queue: QueueActor = QueueActor(
        configuration,
        shard,
        analytics,
        this,
        poolId,
        timer,
        testBundleIdentifier,
        poolProgressAccumulator,
        poolJob,
        context
    )

    private val devices = mutableMapOf<String, SendChannel<DeviceEvent>>()

    private suspend fun notifyDevices() {
        logger.debug { "Notify devices" }
        devices.values.forEach {
            it.safeSend(DeviceEvent.WakeUp)
        }
    }

    private suspend fun onQueueTerminated() {
        devices.values.forEach {
            it.safeSend(DeviceEvent.Terminate)
        }
        terminate()
    }

    private suspend fun deviceReturnedTestBatch(device: Device, batch: TestBatch, reason: String) {
        queue.send(QueueMessage.ReturnBatch(device.toDeviceInfo(), batch, reason))
    }

    private suspend fun deviceCompleted(device: Device, results: TestBatchResults) {
        queue.send(QueueMessage.Completed(device.toDeviceInfo(), results))
    }

    private suspend fun deviceReady(msg: DevicePoolMessage.FromDevice.IsReady) {
        maybeRequestBatch(msg.device)
    }

    // Requests a batch of tests for a random device from the list of devices not running tests at the moment.
    // When @avoidingDevice is not null, attemtps to send the request for any other device whenever available.
    private suspend fun maybeRequestBatch(avoidingDevice: Device? = null) {
        val availableDevices = devices.values.asSequence()
            .map { it as DeviceActor }
            .filter { it.isAvailable }
            .filter { it.device != avoidingDevice }
            .toList()
        if (availableDevices.isEmpty()) {
            if (avoidingDevice != null) {
                devices[avoidingDevice.serialNumber]?.let {
                    val avoidingDeviceActor = it as? DeviceActor
                    if (avoidingDeviceActor?.isAvailable == true) {
                        queue.safeSend(QueueMessage.RequestBatch(avoidingDevice.toDeviceInfo()))
                    }
                }
            }
        } else {
            queue.safeSend(QueueMessage.RequestBatch(availableDevices.shuffled().first().device.toDeviceInfo()))
        }
    }

    private suspend fun executeBatch(device: DeviceInfo, batch: TestBatch) {
        devices[device.serialNumber]?.run {
            safeSend(DeviceEvent.Execute(batch))
        }
    }

    private fun terminate() {
        poolJob.cancel()
        close()
    }

    private suspend fun removeDevice(device: Device) {
        logger.debug { "remove device ${device.serialNumber}" }
        val actor = devices.remove(device.serialNumber)
        actor?.safeSend(DeviceEvent.Terminate)
        logger.debug { "devices.size = ${devices.size}" }
        if (noActiveDevices()) {
            //TODO check if we still have tests and timeout if nothing available
            terminate()
        }
    }

    private fun noActiveDevices() = devices.isEmpty() || devices.all { it.value.isClosedForSend }

    private suspend fun addDevice(device: Device) {
        if (devices.containsKey(device.serialNumber)) {
            logger.warn { "device ${device.serialNumber} already present in pool ${poolId.name}" }
            return
        }

        logger.debug { "add device ${device.serialNumber}" }
        val actor = DeviceActor(poolId, this, configuration, device, poolJob, coroutineContext)
        devices[device.serialNumber] = actor
        actor.safeSend(DeviceEvent.Initialize)
    }
}
