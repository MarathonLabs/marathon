package com.malinskiy.marathon.ios

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.channels.Channel

class iOSDeviceProvider : DeviceProvider {
    override fun initialize(vendorConfiguration: VendorConfiguration) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subscribe() : Channel<DeviceProvider.DeviceEvent> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDevices(): List<Device> {
        TODO("not implemented")
    }

    override fun lockDevice(device: Device): Boolean {
        TODO("not implemented")
    }

    override fun unlockDevice(device: Device): Boolean {
        TODO("not implemented")
    }
}