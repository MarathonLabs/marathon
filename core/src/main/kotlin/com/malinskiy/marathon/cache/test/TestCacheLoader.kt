package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.cache.test.CacheResult.Hit
import com.malinskiy.marathon.cache.test.CacheResult.Miss
import com.malinskiy.marathon.cache.test.key.TestCacheKeyFactory
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSimpleSafeTestName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.system.measureTimeMillis

class TestCacheLoader(
    private val configuration: Configuration,
    private val cache: TestResultsCache,
    private val cacheKeyFactory: TestCacheKeyFactory
) {

    private val logger = MarathonLogging.logger("TestCacheLoader")

    private val _results: Channel<CacheResult> = unboundedChannel()
    val results: ReceiveChannel<CacheResult>
        get() = _results

    private val testsToCheck: Channel<TestToCheck> = unboundedChannel()

    private lateinit var cacheCheckCompleted: Deferred<Unit>

    fun initialize(scope: CoroutineScope) = with(scope) {
        cacheCheckCompleted = async {
            // TODO: check concurrently
            for (test in testsToCheck) {
                var result: CacheResult? = null
                val timeMillis = measureTimeMillis {
                    val cacheKey = cacheKeyFactory.getCacheKey(test.poolId, test.test)

                    logger.debug { "Cache key for ${test.test.toSimpleSafeTestName()}: ${cacheKey.key} ($cacheKey)" }

                    result = cache.load(cacheKey, test.test)?.let {
                        Hit(test.poolId, it)
                    } ?: Miss(test.poolId, TestShard(listOf(test.test)))

                    _results.send(result!!)
                }

                logger.debug {
                    val hitOrMiss = when (result!!) {
                        is Hit -> "hit"
                        is Miss -> "miss"
                    }
                    "Cache $hitOrMiss for ${test.test.toSimpleSafeTestName()}, took $timeMillis milliseconds"
                }
            }
        }
    }

    suspend fun addTests(poolId: DevicePoolId, tests: TestShard) {
        if (configuration.cache.isEnabled) {
            tests.tests.forEach {
                testsToCheck.send(TestToCheck(poolId, it))
            }
        } else {
            _results.send(Miss(poolId, tests))
        }
    }

    suspend fun stop() {
        testsToCheck.close()
        cacheCheckCompleted.await()
        _results.close()
    }

    private class TestToCheck(val poolId: DevicePoolId, val test: Test)
}
