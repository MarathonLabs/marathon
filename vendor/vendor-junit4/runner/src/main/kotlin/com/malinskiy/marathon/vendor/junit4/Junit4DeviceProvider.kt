package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class Junit4DeviceProvider(
    private val timer: Timer
) : DeviceProvider {
    override val deviceInitializationTimeoutMillis: Long = 1_000

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    private val devices: MutableMap<String, Junit4Device> = ConcurrentHashMap()

    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
    }

    override suspend fun terminate() {
        devices.forEach { serial, device ->
            device.dispose()
        }
        channel.close()
    }

    override fun subscribe(): Channel<DeviceProvider.DeviceEvent> {
        runBlocking {
            val count = Runtime.getRuntime().availableProcessors()
            for(i in 0 until count) {
                //For now only local instance is supported
                val localhost = Junit4Device(timer, controlPort = 50051 + i)
                devices["localhost-$i"] = localhost
                channel.send(DeviceProvider.DeviceEvent.DeviceConnected(localhost))
            }
        }

        return channel
    }
}
