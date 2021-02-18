package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class Junit4DeviceProvider : DeviceProvider {
    override val deviceInitializationTimeoutMillis: Long = 1_000

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()

    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
    }

    override suspend fun terminate() {
        channel.close()
    }

    override fun subscribe(): Channel<DeviceProvider.DeviceEvent> {
        runBlocking {
            //For now only local instance is supported
            channel.send(DeviceProvider.DeviceEvent.DeviceConnected(Junit4Device()))
        }

        return channel
    }
}
