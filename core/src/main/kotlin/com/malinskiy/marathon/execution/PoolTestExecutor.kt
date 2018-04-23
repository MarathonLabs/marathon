package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.selects.SelectClause2
import mu.KotlinLogging
import java.util.concurrent.*

class PoolTestExecutor(private val configuration: Configuration,
                       private val tests: Collection<Test>) : SendChannel<PoolMessage> {

    private val logger = KotlinLogging.logger("PoolTestExecutor")

    private val act = actor<PoolMessage> {
        for (msg in channel) {
            when (msg) {
                is PoolMessage.AddDevice -> addDevice(msg)
                is PoolMessage.RemoveDevice -> removeDevice(msg)
                is PoolMessage.Terminate -> terminate()
            }
        }
    }

    private val queue = ConcurrentLinkedQueue<Test>(tests)

    private val executor = Executors.newCachedThreadPool()

    private val devices = mutableMapOf<String, Future<*>>()

    private fun terminate() {
        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)
    }

    private fun removeDevice(msg: PoolMessage.RemoveDevice) {
        val future = devices.remove(msg.device.serialNumber)
        future?.cancel(true)
    }

    private fun addDevice(msg: PoolMessage.AddDevice) {
        val device = msg.device
        launch {
            logger.warn {
                "before prepare device with serial ${device.serialNumber}"
            }
            prepareDevice(device)
            logger.warn {
                "before execute device with serial ${device.serialNumber}"
            }
            execute(device, msg.complete)
        }
    }

    private fun execute(device: Device, complete: Phaser) {
        val future = executor.submit {
            complete.register()
            logger.warn { "Phaser.register" }
            try {
                logger.warn { "queue.isNotEmpty() = ${queue.isNotEmpty()}" }
                while (queue.isNotEmpty()) {
                    logger.warn { "queue.isNotEmpty() = ${queue.isNotEmpty()}" }
                    queue.poll()?.run {
                        device.execute(configuration, TestBatch(listOf(this)))
                    }
                }
            } catch (throwable: Throwable) {
                logger.error(throwable) { "failed" }
                throw throwable
            } finally {
                logger.warn { "Phaser.arriveAndDeregister" }
                complete.arriveAndDeregister()
            }
        }
        devices.put(device.serialNumber,future)
    }

    private fun prepareDevice(device: Device) {
        device.prepare(configuration)
    }

    override val isClosedForSend: Boolean
        get() = act.isClosedForSend
    override val isFull: Boolean
        get() = act.isFull
    override val onSend: SelectClause2<PoolMessage, SendChannel<PoolMessage>>
        get() = act.onSend

    override fun close(cause: Throwable?) = act.close(cause)

    override fun offer(element: PoolMessage) = act.offer(element)

    override suspend fun send(element: PoolMessage) = act.send(element)
}
