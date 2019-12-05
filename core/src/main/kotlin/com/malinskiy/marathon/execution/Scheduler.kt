package com.malinskiy.marathon.execution

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.cache.test.CacheResult
import com.malinskiy.marathon.cache.test.CacheTestReporter
import com.malinskiy.marathon.cache.test.TestCacheLoader
import com.malinskiy.marathon.cache.test.TestCacheSaver
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler.AddDevice
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler.RemoveDevice
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * The logic of scheduler:
 * 1) Subscribe on DeviceProvider
 * 2) Create device pools using PoolingStrategy
 */

class Scheduler(
    private val deviceProvider: DeviceProvider,
    private val cacheLoader: TestCacheLoader,
    private val cacheSaver: TestCacheSaver,
    private val cachedTestsReporter: CacheTestReporter,
    private val analytics: Analytics,
    private val configuration: Configuration,
    private val progressReporter: ProgressReporter,
    private val track: Track,
    context: CoroutineContext
) {

    private val job = Job()
    private val pools = ConcurrentHashMap<DevicePoolId, SendChannel<FromScheduler>>()
    private val poolingStrategy = configuration.poolingStrategy

    private val logger = MarathonLogging.logger("Scheduler")

    private val scope: CoroutineScope = CoroutineScope(context)

    suspend fun initialize() {
        subscribeOnDevices(job)
        subscribeToCacheController()

        cacheLoader.initialize(scope)
        cacheSaver.initialize(scope)

        try {
            withTimeout(deviceProvider.deviceInitializationTimeoutMillis) {
                while (pools.isEmpty()) {
                    delay(100)
                }
            }
        } catch (e: TimeoutCancellationException) {
            job.cancelAndJoin()
            throw NoDevicesException("")
        }
    }

    suspend fun stopAndWaitForCompletion() {
        cacheLoader.stop()

        pools.values.forEach {
            it.send(FromScheduler.RequestStop)
        }

        for (child in job.children) {
            child.join()
        }

        cacheSaver.terminate()
    }

    suspend fun addTests(shard: TestShard) {
        pools.keys.forEach { pool ->
            cacheLoader.addTests(pool, shard)
        }
    }

    private fun subscribeToCacheController() {
        scope.launch {
            for (cacheResult in cacheLoader.results) {
                when (cacheResult) {
                    is CacheResult.Miss -> pools.getValue(cacheResult.pool).send(FromScheduler.AddTests(cacheResult.testShard))
                    is CacheResult.Hit -> cachedTestsReporter.onCachedTest(cacheResult.pool, cacheResult.testResult)
                }
            }
        }
    }

    private fun subscribeOnDevices(job: Job): Job {
        return scope.launch {
            for (msg in deviceProvider.subscribe()) {
                when (msg) {
                    is DeviceProvider.DeviceEvent.DeviceConnected -> {
                        onDeviceConnected(msg, job, coroutineContext)
                    }
                    is DeviceProvider.DeviceEvent.DeviceDisconnected -> {
                        onDeviceDisconnected(msg)
                    }
                }
            }
        }
    }

    private suspend fun onDeviceDisconnected(item: DeviceProvider.DeviceEvent.DeviceDisconnected) {
        val device = item.device
        if (filteredByConfiguration(device)) {
            logger.debug { "device ${device.serialNumber} is filtered out by configuration. skipping disconnect" }
            return
        }

        logger.debug { "device ${device.serialNumber} disconnected" }
        pools.values.forEach {
            it.send(RemoveDevice(device))
        }
    }

    private suspend fun onDeviceConnected(
        item: DeviceProvider.DeviceEvent.DeviceConnected,
        parent: Job,
        context: CoroutineContext
    ) {
        val device = item.device
        if (filteredByConfiguration(device)) {
            logger.debug { "device ${device.serialNumber} is filtered out by configuration. skipping" }
            return
        }

        val poolId = poolingStrategy.associate(device)
        logger.debug { "device ${device.serialNumber} associated with poolId ${poolId.name}" }
        pools.computeIfAbsent(poolId) { id ->
            logger.debug { "pool actor ${id.name} is being created" }
            DevicePoolActor(id, configuration, analytics, progressReporter, track, parent, context)
        }
        pools[poolId]?.send(AddDevice(device)) ?: logger.debug {
            "not sending the AddDevice event " +
                    "to device pool for ${device.serialNumber}"
        }
        track.deviceConnected(poolId, device.toDeviceInfo())
    }

    private fun filteredByConfiguration(device: Device): Boolean {
        val whiteListAccepted = when {
            configuration.includeSerialRegexes.isEmpty() -> true
            else -> configuration.includeSerialRegexes.any { it.matches(device.serialNumber) }
        }
        val blacklistAccepted = when {
            configuration.excludeSerialRegexes.isEmpty() -> true
            else -> configuration.excludeSerialRegexes.none { it.matches(device.serialNumber) }
        }

        return !(whiteListAccepted && blacklistAccepted)
    }
}
