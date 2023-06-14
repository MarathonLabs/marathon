package com.malinskiy.marathon.execution

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler.AddDevice
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler.RemoveDevice
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.progress.PoolProgressAccumulator
import com.malinskiy.marathon.extension.toPoolingStrategy
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
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
    private val analytics: Analytics,
    private val configuration: Configuration,
    private val shard: TestShard,
    private val track: Track,
    private val timer: Timer,
    private val testBundleIdentifier: TestBundleIdentifier?,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    private val job = Job()
    private val pools = ConcurrentHashMap<DevicePoolId, DevicePoolActor>()
    private val results = ConcurrentHashMap<DevicePoolId, PoolProgressAccumulator>()
    private val poolingStrategy = configuration.poolingStrategy.toPoolingStrategy()

    private val logger = MarathonLogging.logger("Scheduler")

    /**
     * @return true if scheduled tests passed successfully, false otherwise
     */
    suspend fun execute() : Boolean {
        subscribeOnDevices(job)
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
        for (child in job.children) {
            child.join()
        }

        return results.all { it.value.aggregateResult() }
    }

    private fun subscribeOnDevices(job: Job): Job {
        return launch {
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
        val accumulator = results.computeIfAbsent(poolId) { id ->
            PoolProgressAccumulator(id, shard, configuration, track)
        }

        pools.computeIfAbsent(poolId) { id ->
            logger.debug { "pool actor ${id.name} is being created" }
            DevicePoolActor(id, configuration, accumulator, analytics, shard, timer, parent, context, testBundleIdentifier)
        }
        pools[poolId]?.send(AddDevice(device)) ?: logger.debug {
            "not sending the AddDevice event " +
                "to device pool for ${device.serialNumber}"
        }
        track.deviceConnected(poolId, device.toDeviceInfo())
    }

    private fun filteredByConfiguration(device: Device): Boolean {
        val allowListAccepted = when {
            configuration.includeSerialRegexes.isEmpty() -> true
            else -> configuration.includeSerialRegexes.any { it.matches(device.serialNumber) }
        }
        val blockListAccepted = when {
            configuration.excludeSerialRegexes.isEmpty() -> true
            else -> configuration.excludeSerialRegexes.none { it.matches(device.serialNumber) }
        }

        return !(allowListAccepted && blockListAccepted)
    }
}
