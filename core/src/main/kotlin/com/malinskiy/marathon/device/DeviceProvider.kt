package com.malinskiy.marathon.device

import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.channels.Channel

interface DeviceProvider {
    sealed class DeviceEvent {
        class DeviceConnected(val device: Device) : DeviceEvent()
        class DeviceDisconnected(val device: Device) : DeviceEvent()
    }

    val deviceInitializationTimeoutMillis: Long
    suspend fun initialize(vendorConfiguration: VendorConfiguration)
    suspend fun terminate()
    fun subscribe(): Channel<DeviceEvent>
}
