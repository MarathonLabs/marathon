package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.selects.SelectClause2
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class PoolTestExecutor(private val configuration: Configuration,
                       private val tests : Collection<Test>) : SendChannel<PoolMessage> {

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

    private val devices = mutableMapOf<String, Future<Void>>()

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
            prepareDevice(device)
            execute(device)
        }
    }

    private fun execute(device: Device) {
        executor.submit {
            while (queue.isNotEmpty()) {
                queue.poll()?.run {
                    device.execute(configuration, TestBatch(listOf(this)))
                }
            }
        }
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
