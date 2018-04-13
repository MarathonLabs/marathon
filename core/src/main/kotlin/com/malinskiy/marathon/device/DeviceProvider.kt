package com.malinskiy.marathon.device

interface DeviceProvider {
    fun getDevices() : List<Device>
    fun lockDevice(device: Device) : Boolean
    fun unlockDevice(device: Device) : Boolean
}