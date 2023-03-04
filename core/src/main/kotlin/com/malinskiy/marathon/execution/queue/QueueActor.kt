package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage.FromQueue
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.progress.PoolProgressAccumulator
import com.malinskiy.marathon.extension.toBatchingStrategy
import com.malinskiy.marathon.extension.toRetryStrategy
import com.malinskiy.marathon.extension.toSortingStrategy
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import java.util.PriorityQueue
import java.util.Queue
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

class QueueActor(
    private val configuration: Configuration,
    private val testShard: TestShard,
    private val analytics: Analytics,
    private val pool: SendChannel<FromQueue>,
    private val poolId: DevicePoolId,
    private val track: Track,
    private val timer: Timer,
    private val testBundleIdentifier: TestBundleIdentifier?,
    private val poolProgressAccumulator: PoolProgressAccumulator,
    poolJob: Job,
    coroutineContext: CoroutineContext
) :
    Actor<QueueMessage>(parent = poolJob, context = coroutineContext) {

    private val logger = MarathonLogging.logger("QueueActor[$poolId]")

    private val sortingStrategy = configuration.sortingStrategy.toSortingStrategy()

    private val queue: Queue<Test> = PriorityQueue(sortingStrategy.process(analytics))
    private val batchingStrategy = configuration.batchingStrategy.toBatchingStrategy()
    private val retryStrategy = configuration.retryStrategy.toRetryStrategy()

    private val activeBatches = mutableMapOf<String, TestBatch>()
    private val uncompletedTestsRetryCount = mutableMapOf<Test, Int>()

    init {
        queue.addAll(testShard.tests + testShard.flakyTests)
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

    private suspend fun onBatchCompleted(device: DeviceInfo, results: TestBatchResults) {
        val finished = results.finished
        val failed = results.failed

        logger.debug { "handle test results ${device.serialNumber}" }
        if (finished.isNotEmpty()) {
            handleFinishedTests(finished, device)
        }
        if (results.uncompleted.isNotEmpty()) {
            handleUncompletedTests(results.uncompleted, device)
        }
        if (failed.isNotEmpty()) {
            handleFailedTests(failed, device)
        }
        activeBatches.remove(device.serialNumber)
        if (queue.isNotEmpty()) {
            pool.send(FromQueue.Notify)
        }
    }

    private fun handleUncompletedTests(uncompletedTests: Collection<TestResult>, device: DeviceInfo) {
        val (uncompletedRetryQuotaExceeded, uncompleted) = uncompletedTests.partition {
            (uncompletedTestsRetryCount[it.test] ?: 0) >= configuration.uncompletedTestRetryQuota
        }

        uncompletedTests.forEach {
            uncompletedTestsRetryCount[it.test] = (uncompletedTestsRetryCount[it.test] ?: 0) + 1
        }

        for (test in uncompletedRetryQuotaExceeded) {
            logger.debug { "uncompletedTestRetryQuota exceeded for ${test.test.toTestName()}}" }
            val testAction = poolProgressAccumulator.testEnded(device, test, final = true)
            processTestAction(testAction, test)
        }

        if (uncompleted.isNotEmpty()) {
            for (testResult in uncompleted) {
                val testAction = poolProgressAccumulator.testEnded(device, testResult, final = false)
                when (testAction) {
                    TestAction.Conclude -> processTestAction(testAction, testResult)
                    null -> rerunTest(testResult.test)
                }
            }
        }
    }

    private suspend fun onReturnBatch(device: DeviceInfo, batch: TestBatch) {
        logger.debug { "onReturnBatch ${device.serialNumber}" }

        val uncompletedTests = batch.tests
        val results = uncompletedTests.map {
            val currentTimeMillis = timer.currentTimeMillis()
            TestResult(
                it,
                device,
                batch.id,
                TestStatus.INCOMPLETE,
                currentTimeMillis,
                currentTimeMillis + 1
            )
        }

        handleUncompletedTests(results, device)
        activeBatches.remove(device.serialNumber)
        if (queue.isNotEmpty()) {
            pool.send(FromQueue.Notify)
        }
    }

    private fun rerunTest(test: Test) {
        queue.add(test)
    }

    private fun onTerminate() {
        close()
    }

    private fun processTestAction(testAction: TestAction?, testResult: TestResult) {
        when (testAction) {
            TestAction.Conclude -> {
                //Test has reached final state. No need to run any of the other retries
                //This doesn't do anything with retries currently in progress
                val oldSize = queue.size
                queue.removeAll(setOf(testResult.test))
                val diff = oldSize - queue.size
                if (diff >= 0) {
                    poolProgressAccumulator.removeTest(testResult.test, diff)
                }
            }

            null -> Unit
        }
    }

    private fun handleFinishedTests(finished: Collection<TestResult>, device: DeviceInfo) {
        finished.forEach {
            val testAction = poolProgressAccumulator.testEnded(device, it)
            processTestAction(testAction, it)
        }
    }

    private fun handleFailedTests(
        failed: Collection<TestResult>,
        device: DeviceInfo
    ) {
        logger.debug { "handle failed tests ${device.serialNumber}" }
        val retryList = retryStrategy.process(poolId, failed, testShard)

        retryList.forEach {
            poolProgressAccumulator.retryTest(it.test)
            val testAction = poolProgressAccumulator.testEnded(device, it)
            processTestAction(testAction, it)
            rerunTest(it.test)
        }

        val (_, noRetries) = failed.partition { testResult ->
            retryList.map { retry -> retry.test }.contains(testResult.test)
        }

        noRetries.forEach {
            val testAction = poolProgressAccumulator.testEnded(device, it)
            processTestAction(testAction, it)
        }
    }

    private suspend fun onRequestBatch(device: DeviceInfo) {
        logger.debug { "request next batch for device ${device.serialNumber}" }
        val queueIsEmpty = queue.isEmpty()

        //Don't separate the condition and the mutator into separate suspending blocks
        if (queue.isNotEmpty() && !activeBatches.containsKey(device.serialNumber)) {
            logger.debug { "sending next batch for device ${device.serialNumber}" }
            val batch = batchingStrategy.process(queue, analytics, testBundleIdentifier)
            activeBatches[device.serialNumber] = batch
            pool.send(FromQueue.ExecuteBatch(device, batch))
            return
        }
        if (queueIsEmpty && activeBatches.isEmpty()) {
            pool.send(FromQueue.Terminated)
            onTerminate()
        } else if (queueIsEmpty) {
            logger.debug {
                "queue is empty but there are active batches present for " + activeBatches.keys.joinToString { it }
            }
        }
    }
}


sealed class QueueMessage {
    data class RequestBatch(val device: DeviceInfo) : QueueMessage()
    data class IsEmpty(val deferred: CompletableDeferred<Boolean>) : QueueMessage()
    data class Completed(val device: DeviceInfo, val results: TestBatchResults) : QueueMessage()
    data class ReturnBatch(val device: DeviceInfo, val batch: TestBatch, val reason: String) : QueueMessage()

    object Terminate : QueueMessage()
}
