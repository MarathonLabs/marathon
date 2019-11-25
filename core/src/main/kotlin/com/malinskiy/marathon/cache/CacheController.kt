package com.malinskiy.marathon.cache

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.coroutines.CoroutineContext

class CacheController(
    private val coroutineContext: CoroutineContext,
    private val cache: TestResultsCache,
    private val cacheMissHandler: ReceiveChannel<Collection<Test>>,
    private val cacheHitHandler: ReceiveChannel<Collection<TestResult>>
) {

    private val logger = MarathonLogging.logger("CacheActor")
    private val scope = CoroutineScope(coroutineContext)

    suspend fun addTests(poolId: DevicePoolId, tests: Collection<Test>) {

    }

    suspend fun onTestFinished(poolId: DevicePoolId, result: TestResult) {

    }

    suspend fun terminate() {

    }
}
