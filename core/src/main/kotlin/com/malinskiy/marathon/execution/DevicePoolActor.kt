package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.healthCheck
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.experimental.channels.Channel
import mu.KotlinLogging

class DevicePoolActor(private val poolId: DevicePoolId,
                      private val configuration: Configuration,
                      private val analytics: Analytics,
                      tests: Collection<Test>) : Actor<DevicePoolMessage>() {

    private val logger = KotlinLogging.logger("DevicePoolActor")

    override suspend fun receive(msg: DevicePoolMessage) {
        when (msg) {
            is DevicePoolMessage.AddDevice -> addDevice(msg)
            is DevicePoolMessage.TestExecutionFinished -> testExecutionFinished(msg)
            is DevicePoolMessage.Ready -> deviceReady(msg)
            is DevicePoolMessage.RemoveDevice -> removeDevice(msg)
            is DevicePoolMessage.Terminate -> terminate()
        }
    }

    private val shardingStrategy = configuration.shardingStrategy
    private val flakinessShard = configuration.flakinessStrategy

    private val shard = flakinessShard.process(shardingStrategy.createShard(tests), analytics)

    private val queue: QueueActor = QueueActor(configuration, shard, analytics)
    private val devices = mutableMapOf<String, Actor<DeviceMessage>>()

    private suspend fun deviceReady(msg: DevicePoolMessage.Ready) {
        val channel = Channel<QueueResponseMessage>()
        queue.send(QueueMessage.RequestNext(channel))
        val response = channel.receive()
        when (response) {
            is QueueResponseMessage.Empty -> msg.sender.send(DeviceMessage.Terminate)
            is QueueResponseMessage.NextBatch -> msg.sender.send(DeviceMessage.ExecuteTestBatch(response.batch))
        }
    }

    private suspend fun testExecutionFinished(msg: DevicePoolMessage.TestExecutionFinished) {
        val channel = Channel<QueueResponseMessage>()
        queue.send(QueueMessage.RequestNext(channel))
        val response = channel.receive()
        when (response) {
            is QueueResponseMessage.Empty -> msg.sender.send(DeviceMessage.Terminate)
            is QueueResponseMessage.NextBatch -> msg.sender.send(DeviceMessage.ExecuteTestBatch(response.batch))
        }
    }

    private var initialized = false

    private fun terminate() {
        close()
    }

    private fun initializeHealthCheck() {
        if (!initialized) {
            healthCheck {
                !devices.values.all { it.isClosedForSend }
            }.invokeOnCompletion {
                terminate()
            }
            initialized = true
        }
    }

    private suspend fun removeDevice(msg: DevicePoolMessage.RemoveDevice) {
        val device = devices.remove(msg.device.serialNumber)
        device?.send(DeviceMessage.Terminate)
    }

    private suspend fun addDevice(msg: DevicePoolMessage.AddDevice) {
        val device = msg.device
        val actor = DeviceActor(poolId, this, configuration, device, analytics)
        devices[device.serialNumber] = actor
        actor.send(DeviceMessage.Initialize)
        initializeHealthCheck()
    }
}
