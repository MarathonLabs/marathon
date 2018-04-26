package com.malinskiy.marathon.execution

import com.malinskiy.marathon.aktor.Aktor
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.healthCheck
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.selects.SelectClause2
import mu.KotlinLogging
import java.lang.RuntimeException
import java.util.concurrent.*

class PoolTestExecutor(private val poolName: String,
                       private val configuration: Configuration,
                       private val tests: Collection<Test>) : Aktor<PoolMessage>() {

    private val logger = KotlinLogging.logger("PoolTestExecutor")

    override suspend fun receive(msg: PoolMessage) {
        when (msg) {
            is PoolMessage.Initialize -> initialize()
            is PoolMessage.AddDevice -> addDevice(msg)
            is PoolMessage.RemoveDevice -> removeDevice(msg)
            is PoolMessage.Terminate -> terminate()
        }
    }

    private val queue = ConcurrentLinkedQueue<Test>(tests)

    private val executor = Executors.newCachedThreadPool()

    private val devices = mutableMapOf<String, Job>()

    private var initialized = false

    private fun terminate() {
        close()
        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)
    }

    private fun initialize() {
        if (!initialized) {
            healthCheck(1_000, 1_000) {
                devices.values.any { it.isActive }
            }.invokeOnCompletion {
                terminate()
            }
            initialized = true
        }
    }

    private fun removeDevice(msg: PoolMessage.RemoveDevice) {
        val job = devices.remove(msg.device.serialNumber)
        job?.cancel()
    }

    private val deviceDispatcher = executor.asCoroutineDispatcher()

    private fun addDevice(msg: PoolMessage.AddDevice) {
        val device = msg.device
        devices[device.serialNumber] = launch(deviceDispatcher) {
            device.prepare(configuration)
            try {
                while (queue.isNotEmpty() && isActive) {
                    queue.poll()?.run {
                        logger.warn { "device = ${device.serialNumber} Pool = $poolName" }
                        device.execute(configuration, TestBatch(listOf(this)))
                    }
                }
            } catch (throwable: Throwable) {
                logger.error(throwable) { "failed" }
                throw throwable
            }
        }
        initialize()
    }
}
