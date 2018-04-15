package com.malinskiy.marathon.device

import com.malinskiy.marathon.vendor.VendorConfiguration

interface DeviceProvider {
    fun initialize(vendorConfiguration: VendorConfiguration)
    fun getDevices() : List<Device>
    fun lockDevice(device: Device) : Boolean
    fun unlockDevice(device: Device) : Boolean
}