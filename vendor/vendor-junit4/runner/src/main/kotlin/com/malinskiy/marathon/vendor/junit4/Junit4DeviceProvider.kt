package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.VendorConfiguration
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class Junit4DeviceProvider(
    private val configuration: Configuration,
    private val timer: Timer
) : DeviceProvider {
    override val deviceInitializationTimeoutMillis: Long = configuration.deviceInitializationTimeoutMillis

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    private val devices: MutableMap<String, Junit4Device> = ConcurrentHashMap()
    private lateinit var testBundleIdentifier: Junit4TestBundleIdentifier
    private var parallelism: Int = 0

    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        val junit4Configuration = vendorConfiguration as Junit4Configuration
        testBundleIdentifier = junit4Configuration.testBundleIdentifier() as Junit4TestBundleIdentifier
        parallelism = junit4Configuration.parallelism
    }

    override suspend fun terminate() {
        devices.forEach { serial, device ->
            device.dispose()
        }
        channel.close()
    }

    override fun subscribe(): Channel<DeviceProvider.DeviceEvent> {
        runBlocking {
            val count = parallelism
            for (i in 0 until count) {
                //For now only local instance is supported
                val localhost = Junit4Device(configuration, timer, testBundleIdentifier, controlPort = 50051 + i)
                devices["localhost-$i"] = localhost
                channel.send(DeviceProvider.DeviceEvent.DeviceConnected(localhost))
            }
        }

        return channel
    }
}
