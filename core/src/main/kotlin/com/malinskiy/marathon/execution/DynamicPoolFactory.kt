package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging

class DynamicPoolFactory(deviceProvider: DeviceProvider,
                         private val poolingStrategy: PoolingStrategy,
                         private val configuration: Configuration,
                         private val list: Collection<Test>) {

    private val logger = KotlinLogging.logger("DynamicPoolFactory")

    private val channel = deviceProvider.subscribe()

    private val pools = mutableMapOf<String, SendChannel<PoolMessage>>()

    suspend fun execute() {
        launch {
            for (msg in channel) {
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

        launch {
            delay(10_000)
            val checkActors = {
                !pools.values.all { it.isClosedForSend }
            }
            while (checkActors()) {
                delay(1_000)
            }
        }.join()
    }

    fun terminate() {
        pools.values.forEach {
            runBlocking {
                if (!it.isClosedForSend) {
                    it.send(PoolMessage.Terminate)
                }
            }
        }
    }

    private suspend fun onDeviceDisconnected(item: DeviceProvider.DeviceEvent.DeviceDisconnected) {
        pools.values.forEach {
            it.send(PoolMessage.RemoveDevice(item.device))
        }
    }

    private suspend fun onDeviceConnected(item: DeviceProvider.DeviceEvent.DeviceConnected) {
        val pools = poolingStrategy.createPools(listOf(item.device))
        pools.forEach {
            this.pools.computeIfAbsent(it.name, { name ->
                PoolTestExecutor(name, configuration, list)
            })
            this.pools[it.name]?.send(PoolMessage.AddDevice(item.device))
        }
    }
}