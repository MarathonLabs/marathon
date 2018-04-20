package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

class DynamicPoolFactory(private val deviceProvider: DeviceProvider,
                         private val poolingStrategy: PoolingStrategy,
                         private val configuration: Configuration,
                         private val list: Collection<Test>) {

    private val channel = deviceProvider.subscribe()

    private val actors = mutableMapOf<String, SendChannel<PoolMessage>>()

    fun execute() {
        launch {
            val item = channel.receive()
            when (item) {
                is DeviceProvider.DeviceEvent.DeviceConnected -> {
                    onDeviceConnected(item)
                }
                is DeviceProvider.DeviceEvent.DeviceDisconnected -> {
                    onDeviceDisconnected(item)
                }
            }
        }
    }

    fun terminate() {
        actors.values.forEach {
            runBlocking {
                it.send(PoolMessage.Terminate)
            }
        }
    }

    private fun onDeviceDisconnected(item: DeviceProvider.DeviceEvent.DeviceDisconnected) {
        runBlocking {
            actors.values.forEach {
                it.send(PoolMessage.RemoveDevice(item.device))
            }
        }
    }

    private fun onDeviceConnected(item: DeviceProvider.DeviceEvent.DeviceConnected) {
        val pools = poolingStrategy.createPools(listOf(item.device))
        pools.forEach {
            actors.computeIfAbsent(it.name, { t -> PoolTestExecutor(configuration, list) })
            actors.computeIfPresent(it.name, { t, u ->
                runBlocking {
                    u.send(PoolMessage.AddDevice(item.device))
                }
                u
            })
        }
    }
}