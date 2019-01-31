package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.actor.safeSend
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.device.DeviceActor
import com.malinskiy.marathon.execution.device.DeviceEvent
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.execution.queue.QueueActor
import com.malinskiy.marathon.execution.queue.QueueMessage
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext

class DevicePoolActor(private val poolId: DevicePoolId,
                      private val configuration: Configuration,
                      analytics: Analytics,
                      tests: Collection<Test>,
                      private val progressReporter: ProgressReporter,
                      parent: Job,
                      context: CoroutineContext) :
        Actor<DevicePoolMessage>(parent = parent, context = context) {

    private val logger = MarathonLogging.logger("DevicePoolActor[${poolId.name}]")

    override suspend fun receive(msg: DevicePoolMessage) {
        when (msg) {
            is DevicePoolMessage.FromScheduler.AddDevice -> addDevice(msg.device)
            is DevicePoolMessage.FromScheduler.RemoveDevice -> removeDevice(msg.device)
            is DevicePoolMessage.FromScheduler.Terminate -> terminate()
            is DevicePoolMessage.FromDevice.IsReady -> deviceReady(msg)
            is DevicePoolMessage.FromDevice.CompletedTestBatch -> deviceCompleted(msg.device, msg.results)
            is DevicePoolMessage.FromDevice.ReturnTestBatch -> deviceReturnedTestBatch(msg.device, msg.batch)
            is DevicePoolMessage.FromQueue.Notify -> notifyDevices()
            is DevicePoolMessage.FromQueue.Terminated -> onQueueTerminated()
            is DevicePoolMessage.FromQueue.ExecuteBatch -> executeBatch(msg.device, msg.batch)
        }
    }

    private val poolJob = Job(parent)

    private val shardingStrategy = configuration.shardingStrategy
    private val flakinessShard = configuration.flakinessStrategy
    private val shard = flakinessShard.process(shardingStrategy.createShard(tests), analytics)

    private val queue: QueueActor = QueueActor(configuration, shard, analytics, this, poolId, progressReporter, poolJob, context)

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

    private suspend fun deviceReturnedTestBatch(device: Device, batch: TestBatch) {
        queue.send(QueueMessage.ReturnBatch(device.toDeviceInfo(), batch))
    }

    private suspend fun deviceCompleted(device: Device, results: TestBatchResults) {
        queue.send(QueueMessage.Completed(device.toDeviceInfo(), results))
    }

    private suspend fun deviceReady(msg: DevicePoolMessage.FromDevice.IsReady) {
        maybeRequestBatch(msg.device)
    }

    private suspend fun maybeRequestBatch(avoiding: Device? = null) {
        val availableDevices = devices.values.asSequence()
            .map { it as DeviceActor }
            .filter { it.isAvailable }
        val device = if (availableDevices.count() == 1) {
            availableDevices.first().device
        } else {
            availableDevices
                    .filter { it.device != avoiding }
                    .toList()
                    .shuffled()
                    .first().device
        }
        queue.safeSend(QueueMessage.RequestBatch(device.toDeviceInfo()))
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
        val actor = DeviceActor(poolId, this, configuration, device, progressReporter, poolJob, coroutineContext)
        devices[device.serialNumber] = actor
        actor.safeSend(DeviceEvent.Initialize)
    }
}
