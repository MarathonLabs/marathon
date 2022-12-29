package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.DevicePoolMessage.FromQueue
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.progress.ProgressReporter
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
    private val progressReporter: ProgressReporter,
    private val track: Track,
    private val timer: Timer,
    private val testBundleIdentifier: TestBundleIdentifier?,
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

    private val testResultReporter = TestResultReporter(poolId, analytics, testShard, configuration, track)

    init {
        val tests = testShard.tests + testShard.flakyTests
        queue.addAll(tests)
        /**
         * Initial queue fill might contain retries from the flakiness strategy
         */
        val expectedTestResults = tests.distinct().size
        progressReporter.testCountExpectation(poolId, expectedTestResults)
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

        if (uncompletedRetryQuotaExceeded.isNotEmpty()) {
            logger.debug { "uncompletedRetryQuotaExceeded for ${uncompletedRetryQuotaExceeded.joinToString(separator = ", ") { it.test.toTestName() }}" }
            val uncompletedToFailed = uncompletedRetryQuotaExceeded.map {
                it.copy(status = TestStatus.FAILURE)
            }
            for (test in uncompletedToFailed) {
                testResultReporter.testIncomplete(device, test, final = true)
            }
        }

        if (uncompleted.isNotEmpty()) {
            for (test in uncompleted) {
                testResultReporter.testIncomplete(device, test, final = false)
            }
            returnTests(uncompleted.map { it.test })
            progressReporter.addRetries(poolId, uncompleted.size)
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

    private fun returnTests(tests: Collection<Test>) {
        queue.addAll(tests)
    }

    private fun onTerminate() {
        close()
    }

    private fun handleFinishedTests(finished: Collection<TestResult>, device: DeviceInfo) {
        finished.filter { testShard.flakyTests.contains(it.test) }.let {
            it.forEach {
                val oldSize = queue.size
                queue.removeAll(listOf(it.test))
                /**
                 * Important edge case:
                 * 1. Multiple runs of test X scheduled via flaky tests
                 * 2. One run of test X finishes and removes other non started flaky retries
                 * 3. Another parallel run of test X finishes and should be counted towards reducing the expected tests
                 */
                val diff = max(oldSize - queue.size, 1)
                testResultReporter.removeTest(it.test, diff)
                progressReporter.removeTests(poolId, diff)
            }
        }
        finished.forEach {
            testResultReporter.testFinished(device, it)
        }
    }

    private fun handleFailedTests(
        failed: Collection<TestResult>,
        device: DeviceInfo
    ) {
        logger.debug { "handle failed tests ${device.serialNumber}" }
        val retryList = retryStrategy.process(poolId, failed, testShard)

        progressReporter.addRetries(poolId, retryList.size)
        queue.addAll(retryList.map { it.test })
        retryList.forEach {
            testResultReporter.retryTest(device, it)
        }

        val (_, noRetries) = failed.partition { testResult ->
            retryList.map { retry -> retry.test }.contains(testResult.test)
        }

        noRetries.forEach {
            testResultReporter.testFailed(device, it)
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
            pool.send(DevicePoolMessage.FromQueue.Terminated)
            onTerminate()
        } else if (queueIsEmpty) {
            logger.debug {
                "queue is empty but there are active batches present for " +
                    "${activeBatches.keys.joinToString { it }}"
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
