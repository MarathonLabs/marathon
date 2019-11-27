package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.cache.test.key.TestCacheKeyFactory
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.coroutines.CoroutineContext

class TestCacheController(
    private val coroutineContext: CoroutineContext,
    private val cache: TestResultsCache,
    private val testCacheKeyProvider: TestCacheKeyFactory,
    private val cacheMissHandler: ReceiveChannel<Collection<Test>>,
    private val cacheHitHandler: ReceiveChannel<Collection<TestResult>>
) {

    private val logger = MarathonLogging.logger("TestCacheController")
    private val scope = CoroutineScope(coroutineContext)

    suspend fun addTests(poolId: DevicePoolId, tests: Collection<Test>) {

    }

    suspend fun onTestFinished(poolId: DevicePoolId, result: TestResult) {

    }

    suspend fun terminate() {

    }
}
