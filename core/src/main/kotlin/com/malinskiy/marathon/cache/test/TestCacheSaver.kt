package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.cache.test.key.TestCacheKeyFactory
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.toSimpleSafeTestName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

class TestCacheSaver(
    private val cache: TestResultsCache,
    private val testCacheKeyProvider: TestCacheKeyFactory
) {

    private val logger = MarathonLogging.logger("TestCacheSaver")

    private val tasks: Channel<SaveTask> = unboundedChannel()
    private lateinit var completableDeferred: Deferred<Unit>

    fun initialize(scope: CoroutineScope) = with(scope) {
        completableDeferred = async {
            for (task in tasks) {
                val storingTime = measureTimeMillis {
                    val cacheKey = testCacheKeyProvider.getCacheKey(task.poolId, task.result.test)
                    cache.store(cacheKey, task.result)
                }
                logger.debug {
                    "Writing test result to cache for ${task.result.test.toSimpleSafeTestName()} took $storingTime milliseconds"
                }
            }
        }
    }

    fun saveTestResult(poolId: DevicePoolId, result: TestResult) = runBlocking {
        // channel is unbounded, so it will return immediately
        tasks.send(SaveTask(poolId, result))
    }

    suspend fun terminate() {
        tasks.close()
        completableDeferred.await()
    }

    private class SaveTask(val poolId: DevicePoolId, val result: TestResult)
}
