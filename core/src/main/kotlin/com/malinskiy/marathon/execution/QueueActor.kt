package com.malinskiy.marathon.execution

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage.FromQueue
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import mu.KotlinLogging
import java.util.*

class QueueActor(configuration: Configuration,
                 private val testShard: TestShard,
                 metricsProvider: MetricsProvider,
                 private val pool: SendChannel<FromQueue>,
                 private val poolId: DevicePoolId,
                 private val progressReporter: ProgressReporter,
                 poolJob: Job) : Actor<QueueMessage>(parent = poolJob) {

    private val logger = KotlinLogging.logger("QueueActor[$poolId]")

    private val sorting = configuration.sortingStrategy

    private val queue: Queue<Test> = PriorityQueue<Test>(sorting.process(metricsProvider))
    private val batching = configuration.batchingStrategy
    private val retry = configuration.retryStrategy

    private val activeBatches = mutableMapOf<Device, TestBatch>()

    init {
        queue.addAll(testShard.tests + testShard.flakyTests)
        progressReporter.totalTests(poolId, queue.size)
    }

    override suspend fun receive(msg: QueueMessage) {
        when (msg) {
            is QueueMessage.RequestBatch -> {
                onRequestBatch(msg.device)
            }
            is QueueMessage.IsEmpty -> {
                msg.deferred.complete(queue.isEmpty() && activeBatches.isEmpty())
            }
            is QueueMessage.Terminate -> {
                onTerminate()
            }
            is QueueMessage.Completed -> {
                onBatchCompleted(msg.device, msg.results)
            }
            is QueueMessage.ReturnBatch -> {
                onReturnBatch(msg.device, msg.batch)
            }
        }
    }

    private suspend fun onBatchCompleted(device: Device, results: TestBatchResults) {
        val finished = results.finished
        val failed = results.failed
        logger.debug { "handle test results ${device.serialNumber}" }
        if (finished.isNotEmpty()) {
            handleFinishedTests(finished)
        }
        if (failed.isNotEmpty()) {
            handleFailedTests(poolId, failed, device)
        }
        activeBatches.remove(device)
        onRequestBatch(device)
    }

    private fun onReturnBatch(device: Device, batch: TestBatch) {
        queue.addAll(batch.tests)
        activeBatches.remove(device)
    }

    private fun onTerminate() {
        close()
    }

    private fun handleFinishedTests(finished: Collection<Test>) {
        finished.filter { testShard.flakyTests.contains(it) }.let {
            val oldSize = queue.size
            queue.removeAll(it)
            progressReporter.removeTests(poolId, oldSize - queue.size)
        }
    }

    private suspend fun handleFailedTests(poolId: DevicePoolId,
                                          failed: Collection<Test>,
                                          device: Device) {
        logger.debug { "handle failed tests ${device.serialNumber}" }
        val retryList = retry.process(poolId, failed, testShard)
        queue.addAll(retryList)
        if (retryList.isNotEmpty()) {
            pool.send(FromQueue.Notify)
        }
    }


    private suspend fun onRequestBatch(device: Device) {
        logger.debug { "request next batch for device ${device.serialNumber}" }
        val queueIsEmpty = queue.isEmpty()
        if (queue.isNotEmpty() && !activeBatches.containsKey(device)) {
            sendBatch(device)
            return
        }
        if (queueIsEmpty && activeBatches.isEmpty()) {
            pool.send(DevicePoolMessage.FromQueue.Terminated)
            onTerminate()
        }
    }

    private suspend fun sendBatch(device: Device) {
        val batch = batching.process(queue)
        activeBatches[device] = batch
        pool.send(FromQueue.ExecuteBatch(device, batch))
    }
}


sealed class QueueMessage {
    data class RequestBatch(val device: Device) : QueueMessage()
    data class IsEmpty(val deferred: CompletableDeferred<Boolean>) : QueueMessage()
    data class Completed(val device: Device, val results: TestBatchResults) : QueueMessage()
    data class ReturnBatch(val device: Device, val batch: TestBatch) : QueueMessage()

    object Terminate : QueueMessage()
}
