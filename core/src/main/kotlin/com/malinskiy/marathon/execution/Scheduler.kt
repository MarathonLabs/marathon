package com.malinskiy.marathon.execution

import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler.AddDevice
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler.RemoveDevice
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.waitWhileTrue
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging

/**
 * The logic of scheduler:
 * 1) Subscribe on DeviceProvider
 * 2) Create device pools using PoolingStrategy
 */
private const val DEFAULT_INITIAL_DELAY_MILLIS = 10_000L

class Scheduler(private val deviceProvider: DeviceProvider,
                private val analytics: Analytics,
                private val configuration: Configuration,
                private val tests: Collection<Test>) {

    private val pools = mutableMapOf<DevicePoolId, SendChannel<FromScheduler>>()
    private val poolingStrategy = configuration.poolingStrategy

    private val logger = KotlinLogging.logger("Scheduler")

    suspend fun execute() {
        subscribeOnDevices()
        waitWhileTrue(startDelay = DEFAULT_INITIAL_DELAY_MILLIS) {
            logger.debug { "waiting for completion" }
            !pools.values.all { it.isClosedForSend }
        }.join()
    }

    fun getPools(): List<DevicePoolId> {
        return pools.keys.toList()
    }

    private fun subscribeOnDevices() {
        launch {
            for (msg in deviceProvider.subscribe()) {
                when (msg) {
                    is DeviceProvider.DeviceEvent.DeviceConnected -> {
                        onDeviceConnected(msg)
                    }
                    is DeviceProvider.DeviceEvent.DeviceDisconnected -> {
                        onDeviceDisconnected(msg)
                    }
                }
            }
        }
    }

    private suspend fun onDeviceDisconnected(item: DeviceProvider.DeviceEvent.DeviceDisconnected) {
        logger.debug { "device ${item.device.serialNumber} disconnected" }
        pools.values.forEach {
            it.send(RemoveDevice(item.device))
        }
    }

    private suspend fun onDeviceConnected(item: DeviceProvider.DeviceEvent.DeviceConnected) {
        val device = item.device
        val poolId = poolingStrategy.associate(device)
        logger.debug { "device ${device.serialNumber} associated with poolId ${poolId.name}" }
        pools.computeIfAbsent(poolId) { id ->
            DevicePoolActor(id, configuration, analytics, tests)
        }
        pools[poolId]?.send(AddDevice(device))
        analytics.trackDeviceConnected(poolId, device)
    }
}
