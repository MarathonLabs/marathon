package com.malinskiy.marathon.execution

import com.malinskiy.marathon.aktor.Aktor
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.healthCheck
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import mu.KotlinLogging
import java.util.concurrent.*

class DevicePoolAktor(private val poolId: DevicePoolId,
                      private val configuration: Configuration,
                      private val tests: Collection<Test>) : Aktor<PoolMessage>() {

    private val logger = KotlinLogging.logger("DevicePoolAktor")

    override suspend fun receive(msg: PoolMessage) {
        when (msg) {
            is PoolMessage.AddDevice -> addDevice(msg)
            is PoolMessage.TestExecutionFinished -> testExecutionFinished(msg)
            is PoolMessage.Ready -> deviceReady(msg)
            is PoolMessage.RemoveDevice -> removeDevice(msg)
            is PoolMessage.Terminate -> terminate()
        }
    }

    private suspend fun deviceReady(msg: PoolMessage.Ready) {
        if (queue.isNotEmpty()) {
            msg.sender.send(DeviceMessage.ExecuteTestBatch(queue.poll()))
        } else {
            msg.sender.send(DeviceMessage.Terminate)
        }
    }

    private suspend fun testExecutionFinished(msg: PoolMessage.TestExecutionFinished) {
        if (queue.isNotEmpty()) {
            msg.sender.send(DeviceMessage.ExecuteTestBatch(queue.poll()))
        } else {
            msg.sender.send(DeviceMessage.Terminate)
        }
    }

    private val queue = ConcurrentLinkedQueue<TestBatch>(configuration.batchingStrategy.process(listOf(TestShard(tests))))

    private val executor = Executors.newCachedThreadPool()

    private val devices = mutableMapOf<String, DeviceAktor>()

    private var initialized = false

    private fun terminate() {
        close()
        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)
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

    private suspend fun removeDevice(msg: PoolMessage.RemoveDevice) {
        val device = devices.remove(msg.device.serialNumber)
        device?.send(DeviceMessage.Terminate)
    }

    private suspend fun addDevice(msg: PoolMessage.AddDevice) {
        val device = msg.device
        val aktor = DeviceAktor(this, configuration, device)
        devices[device.serialNumber] = aktor
        aktor.send(DeviceMessage.Initialize)
        initializeHealthCheck()
    }
}
