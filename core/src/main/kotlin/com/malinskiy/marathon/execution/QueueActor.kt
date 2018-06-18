package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage.FromQueue
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.CompletableDeferred
import java.util.Queue
import java.util.LinkedList

class QueueActor(configuration: Configuration,
                 private val testShard: TestShard,
                 private val metricsProvider: MetricsProvider,
                 private val pool: Actor<DevicePoolMessage.FromQueue>) : Actor<QueueMessage>() {

    private val queue: Queue<Test> = LinkedList<Test>(testShard.tests)
    private val sorting = configuration.sortingStrategy
    private val batching = configuration.batchingStrategy
    private val retry = configuration.retryStrategy

    override suspend fun receive(msg: QueueMessage) {
        when (msg) {
            is QueueMessage.RequestNext -> {
                requestNextBatch(msg.channel, msg.device)
            }
            is QueueMessage.IsEmpty -> {
                msg.deffered.complete(queue.isEmpty())
            }
            is QueueMessage.FromDevice.TestFailed -> {
                handleFailedTests(msg.devicePoolId, msg.failed, msg.device)
            }
            is QueueMessage.Terminate -> {

            }
        }
    }

    private suspend fun handleFailedTests(poolId: DevicePoolId, failed: Collection<Test>, device: Device) {
        val retryList = retry.process(poolId, failed, testShard)
        queue.addAll(retryList)
        if (retryList.isNotEmpty()) {
            pool.send(FromQueue.Notify)
        }
    }

    private fun requestNextBatch(deferred: CompletableDeferred<QueueResponseMessage>, device: Device) {
        if (queue.isNotEmpty()) {
            sendBatch(deferred)
        } else {
            deferred.complete(QueueResponseMessage.Wait)
        }
    }

    private fun sendBatch(channel: CompletableDeferred<QueueResponseMessage>) {
        val sort = sorting.process(queue, metricsProvider)
        val batch = batching.process(LinkedList(sort))
        batch.tests.forEach {
            queue.remove(it)
        }
        channel.complete(QueueResponseMessage.NextBatch(batch))
    }
}

sealed class QueueResponseMessage {
    data class NextBatch(val batch: TestBatch) : QueueResponseMessage()
    object Wait : QueueResponseMessage()
}

sealed class QueueMessage {
    data class RequestNext(val channel: CompletableDeferred<QueueResponseMessage>,
                           val device: Device) : QueueMessage()

    data class IsEmpty(val deffered: CompletableDeferred<Boolean>) : QueueMessage()
    sealed class FromDevice : QueueMessage() {
        data class TestFailed(val devicePoolId: DevicePoolId,
                              val failed: Collection<Test>,
                              val device: Device) : FromDevice()
    }

    object Terminate : QueueMessage()
}
