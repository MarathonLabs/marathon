package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage.FromQueue
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.waitWhileTrue
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.channels.SendChannel
import mu.KotlinLogging

class DevicePoolActor(private val poolId: DevicePoolId,
                      private val configuration: Configuration,
                      private val analytics: Analytics,
                      tests: Collection<Test>,
                      private val progressReporter: ProgressReporter) : Actor<DevicePoolMessage>() {

    private val logger = KotlinLogging.logger("DevicePoolActor[${poolId.name}]")

    override suspend fun receive(msg: DevicePoolMessage) {
        when (msg) {
            is FromScheduler.AddDevice -> addDevice(msg.device)
            is FromScheduler.RemoveDevice -> removeDevice(msg.device)
            is FromScheduler.Terminate -> terminate()
            is FromDevice.Ready -> deviceReady(msg)
            is FromDevice.Failed -> deviceFailed(msg.device)
            is FromQueue.Notify -> notifyDevices()
            is FromQueue.ExecuteBatch -> executeBatch(msg.device, msg.batch)
        }
    }

    private val shardingStrategy = configuration.shardingStrategy
    private val flakinessShard = configuration.flakinessStrategy
    private val shard = flakinessShard.process(shardingStrategy.createShard(tests), analytics)

    private val queue: QueueActor = QueueActor(configuration, shard, analytics, this, poolId, progressReporter)

    private val devices = mutableMapOf<String, SendChannel<DeviceMessage>>()
    private val deviceStatuses = mutableMapOf<String, DeviceStatus>()

    private var initialized = false

    private suspend fun notifyDevices() {
        logger.debug { "Notify devices" }
        devices.filter {
            !it.value.isClosedForSend
        }.filter {
            deviceStatuses[it.key] != DeviceStatus.RUNNING
        }.forEach {
            it.value.send(DeviceMessage.WakeUp)
        }
    }

    private suspend fun deviceReady(msg: FromDevice.Ready) {
        updateDeviceStatus(msg.device, DeviceStatus.READY)
        queue.send(QueueMessage.RequestNext(msg.device))
    }

    private suspend fun deviceFailed(device: Device) {
        removeDevice(device)
    }

    private fun updateDeviceStatus(device: Device, status: DeviceStatus) {
        deviceStatuses[device.serialNumber] = status
    }

    private suspend fun executeBatch(device: Device, batch: TestBatch) {
        devices[device.serialNumber]?.let {
            updateDeviceStatus(device, DeviceStatus.RUNNING)
            it.send(DeviceMessage.Execute(batch))
        }
    }

    private fun terminate() {
        close()
    }

    private fun allClosed() = devices.values.all { it.isClosedForSend }

    private fun anyRunning(): Boolean {
        return deviceStatuses.values.any { it == DeviceStatus.RUNNING }
    }

    private suspend fun queueIsEmpty(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        queue.send(QueueMessage.IsEmpty(deferred))
        return deferred.await()
    }

    private suspend fun checkStatus() {
        if (queueIsEmpty() && !anyRunning()) {
            devices.values.forEach {
                it.send(DeviceMessage.Terminate)
            }
        }
    }

    private fun initializeHealthCheck() {
        if (!initialized) {
            waitWhileTrue(startDelay = 10_000) {
                checkStatus()
                !allClosed()
            }.invokeOnCompletion {
                terminate()
            }
            initialized = true
        }
    }

    private suspend fun removeDevice(device: Device) {
        logger.debug { "remove device ${device.serialNumber}" }
        val actor = devices.remove(device.serialNumber)
        deviceStatuses.remove(device.serialNumber)
        actor?.send(DeviceMessage.Terminate)
        logger.debug { "devices.size = ${devices.size}" }
        logger.debug { "deviceStatuses.size = ${deviceStatuses.size}" }
    }

    private suspend fun addDevice(device: Device) {
        logger.debug { "add device ${device.serialNumber}" }
        val actor = DeviceActor(poolId, this, configuration, device, analytics, queue, progressReporter)
        devices[device.serialNumber] = actor
        deviceStatuses[device.serialNumber] = DeviceStatus.CONNECTED
        actor.send(DeviceMessage.Initialize)
        initializeHealthCheck()
    }
}
