package com.malinskiy.marathon.ios.idb

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.channels.Channel

class IOSDeviceProvider : DeviceProvider {
    override val deviceInitializationTimeoutMillis: Long
        get() = TODO("Not yet implemented")

    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        TODO("Not yet implemented")
    }

    override suspend fun terminate() {
        TODO("Not yet implemented")
    }

    override fun subscribe(): Channel<DeviceProvider.DeviceEvent> {
        TODO("Not yet implemented")
    }
}
