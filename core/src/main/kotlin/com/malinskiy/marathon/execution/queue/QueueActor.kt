package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.DevicePoolMessage.FromQueue
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import java.util.*
import kotlin.coroutines.CoroutineContext

class QueueActor(private val configuration: Configuration,
                 private val testShard: TestShard,
                 private val analytics: Analytics,
                 private val pool: SendChannel<FromQueue>,
                 private val poolId: DevicePoolId,
                 private val progressReporter: ProgressReporter,
                 poolJob: Job,
                 coroutineContext: CoroutineContext) :
        Actor<QueueMessage>(parent = poolJob, context = coroutineContext) {

    private val logger = MarathonLogging.logger("QueueActor[$poolId]")

    private val sorting = configuration.sortingStrategy

    private val queue: Queue<Test> = PriorityQueue<Test>(sorting.process(analytics))
    private val batching = configuration.batchingStrategy
    private val retry = configuration.retryStrategy

    private val activeBatches = mutableMapOf<String, TestBatch>()
    private val uncompletedTestsRetryCount = mutableMapOf<Test, Int>()

    private val testResultReporter = TestResultReporter(poolId, analytics, testShard, configuration)

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
        }
    }

    private suspend fun onBatchCompleted(device: DeviceInfo, results: TestBatchResults) {
        logger.debug { "handle test results ${device.serialNumber}" }

        if (results.passed.isNotEmpty()) {
            handlePassedTests(results.passed, device)
        }
        if (results.failed.isNotEmpty()) {
            handleFailedTests(results.failed, device)
        }
        if (results.incomplete.isNotEmpty()) {
            handleProvenUncompletedTests(results.incomplete, device)
        }
        // there is no statistics if can test be missed by its quality
        // so by default missed test is incomplete also
        if (results.missed.isNotEmpty()) {
            handleIncompleteTests(results.missed, device)
        }
        activeBatches.remove(device.serialNumber)
    }

    private fun onTerminate() {
        close()
    }

    private suspend fun handleProvenUncompletedTests(incomeIncompleted: Collection<TestResult>, device: DeviceInfo) {
        incomeIncompleted.forEach {
            testResultReporter.testIncomplete(device, it)
        }
        handleIncompleteTests(incomeIncompleted, device)
    }

    private suspend fun handleIncompleteTests(incomeIncompleted: Collection<TestResult>, device: DeviceInfo) {
        val (retryQuotaExceeded, hasChance) = incomeIncompleted.partition {
            (uncompletedTestsRetryCount[it.test] ?: 0) >= configuration.uncompletedTestRetryQuota
        }

        if (retryQuotaExceeded.isNotEmpty()) {
            handleFailedTests(retryQuotaExceeded, device)
        }

        if (hasChance.isNotEmpty()) {
            hasChance.forEach {
                uncompletedTestsRetryCount[it.test] = (uncompletedTestsRetryCount[it.test] ?: 0) + 1
            }
            queue.addAll(hasChance.map { it.test })
        }
    }

    private fun handlePassedTests(passed: Collection<TestResult>, device: DeviceInfo) {
        val flakyThatPassed = passed.filter { testShard.flakyTests.contains(it.test) }

        passed.forEach {
            testResultReporter.testPassed(device, it)
        }
        flakyThatPassed.forEach {
            val oldSize = queue.size
            queue.removeAll(listOf(it.test))
            val diff = oldSize - queue.size
            testResultReporter.removeTest(it.test, diff)
            progressReporter.removeTests(poolId, diff)
        }
    }

    private suspend fun handleFailedTests(failed: Collection<TestResult>,
                                          device: DeviceInfo) {
        val retryList = retry.process(poolId, failed, testShard)

        progressReporter.addTests(poolId, retryList.size)
        queue.addAll(retryList.map { it.test })
        if (retryList.isNotEmpty()) {
            pool.send(FromQueue.Notify)
        }

        retryList.forEach {
            testResultReporter.retryTest(device, it)
        }

        failed.filterNot {
            retryList.map { it.test }.contains(it.test)
        }.forEach {
            testResultReporter.testFailed(device, it)
        }
    }


    private suspend fun onRequestBatch(device: DeviceInfo) {
        logger.debug { "request next batch for device ${device.serialNumber}" }

        if (queue.isNotEmpty()) {
            if (activeBatches.containsKey(device.serialNumber)) {
                return
            }

            logger.debug { "sending next batch for device ${device.serialNumber}" }
            sendBatch(device)
            return
        }

        // queue is empty
        if (activeBatches.isEmpty()) {
            pool.send(DevicePoolMessage.FromQueue.Terminated)
            onTerminate()
            return
        }

        logger.debug {
            "queue is empty but there are active batches present for " +
                    "${activeBatches.keys.joinToString { it }}"
        }
    }

    private suspend fun sendBatch(device: DeviceInfo) {
        val batch = batching.process(queue, analytics)
        activeBatches[device.serialNumber] = batch
        pool.send(FromQueue.ExecuteBatch(device, batch))
    }
}


sealed class QueueMessage {
    data class RequestBatch(val device: DeviceInfo) : QueueMessage()
    data class IsEmpty(val deferred: CompletableDeferred<Boolean>) : QueueMessage()
    data class Completed(val device: DeviceInfo, val results: TestBatchResults) : QueueMessage()
//    data class ReturnBatch(val device: DeviceInfo, val batch: TestBatch) : QueueMessage()

    object Terminate : QueueMessage()
}
