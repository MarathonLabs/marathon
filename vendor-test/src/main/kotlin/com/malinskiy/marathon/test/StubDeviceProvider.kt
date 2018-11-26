package com.malinskiy.marathon.test

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch

class StubDeviceProvider : DeviceProvider {
    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    var providingLogic: (suspend (Channel<DeviceProvider.DeviceEvent>) -> Unit)? = null

    override fun initialize(vendorConfiguration: VendorConfiguration) {
    }

    override fun subscribe(): Channel<DeviceProvider.DeviceEvent> {
        providingLogic?.let {
            launch {
                providingLogic?.invoke(channel)
            }
        }

        return channel
    }

    override fun lockDevice(device: Device): Boolean {
        TODO("Not implemented")
    }

    override fun unlockDevice(device: Device): Boolean {
        TODO("Not implemented")
    }

    override fun terminate() {
    }
}