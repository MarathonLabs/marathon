package com.malinskiy.marathon.ios

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.channels.Channel

class IOSDeviceProvider : DeviceProvider {
    override fun terminate() {
    }

    override fun initialize(vendorConfiguration: VendorConfiguration) {
    }

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    override fun subscribe() = channel

    override fun lockDevice(device: Device): Boolean {
        return false
    }

    override fun unlockDevice(device: Device): Boolean {
        return false
    }
}
