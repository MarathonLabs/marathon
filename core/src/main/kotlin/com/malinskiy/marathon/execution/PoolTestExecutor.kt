package com.malinskiy.marathon.execution

import com.malinskiy.marathon.aktor.Aktor
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.selects.SelectClause2
import mu.KotlinLogging
import java.util.concurrent.*

class PoolTestExecutor(private val configuration: Configuration,
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

    private val devices = mutableMapOf<String, Future<*>>()

    private fun terminate() {
        logger.error { "PoolTestExecutor.terminate" }
        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)
        close()
    }

    private fun initialize() {
        launch {
            delay(500)
            val checkDevices = { !devices.values.all { it.isDone or it.isCancelled } }
            while (checkDevices()) {
                devices.forEach { t, u ->
                    logger.error("$t = ${u.isDone}")
                }
                delay(1_000)
            }
            terminate()
        }
    }

    private fun removeDevice(msg: PoolMessage.RemoveDevice) {
        val future = devices.remove(msg.device.serialNumber)
        future?.cancel(true)
    }

    private fun addDevice(msg: PoolMessage.AddDevice) {
        val device = msg.device
        launch(executor.asCoroutineDispatcher()) {
            prepareDevice(device)
            execute(device)
        }
    }

    private fun execute(device: Device) {
        try {
            while (queue.isNotEmpty()) {
                queue.poll()?.run {
                    logger.warn { "device = ${device.serialNumber} Thread.currentThread()  = ${Thread.currentThread()}" }
                    device.execute(configuration, TestBatch(listOf(this)))
                }
            }
        } catch (throwable: Throwable) {
            logger.error(throwable) { "failed" }
            throw throwable
        }
    }

    private fun prepareDevice(device: Device) {
        device.prepare(configuration)
    }
}
