package com.malinskiy.marathon.execution

import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler.AddDevice
import com.malinskiy.marathon.execution.DevicePoolMessage.FromScheduler.RemoveDevice
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.waitWhileTrue
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.joinChildren
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging

/**
 * The logic of scheduler:
 * 1) Subscribe on DeviceProvider
 * 2) Create device pools using PoolingStrategy
 */

class Scheduler(private val deviceProvider: DeviceProvider,
                private val analytics: Analytics,
                private val configuration: Configuration,
                private val tests: Collection<Test>,
                private val progressReporter: ProgressReporter) {

    private val pools = mutableMapOf<DevicePoolId, SendChannel<FromScheduler>>()
    private val poolingStrategy = configuration.poolingStrategy

    private val logger = KotlinLogging.logger("Scheduler")

    suspend fun execute() {
        val job = Job()
        subscribeOnDevices(job)
        /*launch {
            delay(10_000)
        }.join()*/
        Thread.sleep(10_000)
        job.joinChildren()
    }

    fun getPools(): List<DevicePoolId> {
        return pools.keys.toList()
    }

    private fun subscribeOnDevices(job: Job) {
        launch {
            for (msg in deviceProvider.subscribe()) {
                when (msg) {
                    is DeviceProvider.DeviceEvent.DeviceConnected -> {
                        onDeviceConnected(msg, job)
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

    private suspend fun onDeviceConnected(item: DeviceProvider.DeviceEvent.DeviceConnected, parent: Job) {
        val device = item.device
        val poolId = poolingStrategy.associate(device)
        logger.debug { "device ${device.serialNumber} associated with poolId ${poolId.name}" }
        pools.computeIfAbsent(poolId) { id ->
            DevicePoolActor(id, configuration, analytics, tests, progressReporter, parent)
        }
        pools[poolId]?.send(AddDevice(device))
        analytics.trackDeviceConnected(poolId, device)
    }
}
