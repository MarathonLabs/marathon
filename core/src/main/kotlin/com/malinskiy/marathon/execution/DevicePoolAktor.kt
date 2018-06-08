package com.malinskiy.marathon.execution

import com.malinskiy.marathon.aktor.Aktor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.healthCheck
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

class DevicePoolAktor(private val poolId: DevicePoolId,
                      private val configuration: Configuration,
                      private val analytics: Analytics,
                      private val shard: TestShard) : Aktor<DevicePoolMessage>() {

    private val logger = KotlinLogging.logger("DevicePoolAktor")

    override suspend fun receive(msg: DevicePoolMessage) {
        when (msg) {
            is DevicePoolMessage.AddDevice -> addDevice(msg)
            is DevicePoolMessage.TestExecutionFinished -> testExecutionFinished(msg)
            is DevicePoolMessage.Ready -> deviceReady(msg)
            is DevicePoolMessage.RemoveDevice -> removeDevice(msg)
            is DevicePoolMessage.Terminate -> terminate()
        }
    }

    private suspend fun deviceReady(msg: DevicePoolMessage.Ready) {
        if (queue.isNotEmpty()) {
            msg.sender.send(DeviceMessage.ExecuteTestBatch(queue.poll()))
        } else {
            msg.sender.send(DeviceMessage.Terminate)
        }
    }

    private suspend fun testExecutionFinished(msg: DevicePoolMessage.TestExecutionFinished) {
        if (queue.isNotEmpty()) {
            msg.sender.send(DeviceMessage.ExecuteTestBatch(queue.poll()))
        } else {
            msg.sender.send(DeviceMessage.Terminate)
        }
    }

    private val shardingStrategy = configuration.shardingStrategy
    private val batchingStrategy = configuration.batchingStrategy

    private val queue = ConcurrentLinkedQueue<TestBatch>(batchingStrategy.process(shard))

    private val devices = mutableMapOf<String, Aktor<DeviceMessage>>()

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
        val aktor = DeviceAktor(poolId, this, configuration, device, analytics)
        devices[device.serialNumber] = aktor
        aktor.send(DeviceMessage.Initialize)
        initializeHealthCheck()
    }
}
