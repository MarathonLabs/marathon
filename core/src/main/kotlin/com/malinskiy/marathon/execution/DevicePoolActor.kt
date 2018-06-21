package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler
import com.malinskiy.marathon.execution.DevicePoolMessage.FromQueue
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice
import com.malinskiy.marathon.waitWhileTrue
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.channels.SendChannel
import mu.KotlinLogging

class DevicePoolActor(private val poolId: DevicePoolId,
                      private val configuration: Configuration,
                      private val analytics: Analytics,
                      tests: Collection<Test>) : Actor<DevicePoolMessage>() {

    private val logger = KotlinLogging.logger("DevicePoolActor[${poolId.name}]")

    override suspend fun receive(msg: DevicePoolMessage) {
        when (msg) {
            is FromScheduler.AddDevice -> addDevice(msg)
            is FromScheduler.RemoveDevice -> removeDevice(msg)
            is FromScheduler.Terminate -> terminate()
            is FromDevice.RequestNextBatch -> testExecutionFinished(msg)
            is FromQueue.Notify -> notifyDevices()
        }
    }

    private val shardingStrategy = configuration.shardingStrategy
    private val flakinessShard = configuration.flakinessStrategy

    private val shard = flakinessShard.process(shardingStrategy.createShard(tests), analytics)

    private val queue: QueueActor = QueueActor(configuration, shard, analytics, this, poolId)

    private val devices = mutableMapOf<String, SendChannel<DeviceMessage>>()

    private suspend fun notifyDevices() {
        logger.debug { "Notify devices" }
        devices.filterValues { it.isClosedForSend }.forEach {
            it.value.send(DeviceMessage.WakeUp)
        }
    }

    private suspend fun testExecutionFinished(msg: FromDevice.RequestNextBatch) {
        logger.debug { "test execution finished ${msg.device.serialNumber}" }
        val deferred = CompletableDeferred<QueueResponseMessage>()
        queue.send(QueueMessage.RequestNext(deferred, msg.device))
        val response = deferred.await()
        when (response) {
            is QueueResponseMessage.Wait -> msg.sender.send(DeviceMessage.Wait)
            is QueueResponseMessage.NextBatch -> msg.sender.send(DeviceMessage.ExecuteTestBatch(response.batch))
        }
    }

    private var initialized = false

    private fun terminate() {
        close()
    }

    private fun allClosed() = devices.values.all { it.isClosedForSend }

    private suspend fun anyRunning(): Boolean {
        return devices.values.any {
            val referred = CompletableDeferred<DeviceStatus>()
            it.send(DeviceMessage.GetStatus(referred))
            val status = referred.await()
            status == DeviceStatus.RUNNING
        }
    }

    private suspend fun queueIsEmpty(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        queue.send(QueueMessage.IsEmpty(deferred))
        return deferred.await()
    }

    private fun initializeHealthCheck() {
        if (!initialized) {
            waitWhileTrue(startDelay = 10_000) {
                logger.debug { "waiting for completion" }
                !allClosed() && anyRunning() && !queueIsEmpty()
            }.invokeOnCompletion {
                terminate()
            }
            initialized = true
        }
    }

    private suspend fun removeDevice(msg: FromScheduler.RemoveDevice) {
        logger.debug { "remove device ${msg.device.serialNumber}" }
        val device = devices.remove(msg.device.serialNumber)
        device?.send(DeviceMessage.Terminate)
    }

    private suspend fun addDevice(msg: FromScheduler.AddDevice) {
        logger.debug { "Notify devices" }
        val device = msg.device
        val actor = DeviceActor(poolId, this, configuration, device, analytics, queue)
        devices[device.serialNumber] = actor
        actor.send(DeviceMessage.Initialize)
        initializeHealthCheck()
    }
}
