package com.malinskiy.marathon.device

interface DeviceProviderFactory {
    fun create(): DeviceProvider
}
