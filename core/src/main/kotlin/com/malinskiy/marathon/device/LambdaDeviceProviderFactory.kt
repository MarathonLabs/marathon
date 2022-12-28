package com.malinskiy.marathon.device

class LambdaDeviceProviderFactory(private val block: () -> DeviceProvider) : DeviceProviderFactory {
    override fun create() = block()
}
