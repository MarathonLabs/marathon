package com.malinskiy.marathon.device

import kotlinx.coroutines.channels.Channel

interface DeviceProvider {
    sealed class DeviceEvent {
        class DeviceConnected(val device: Device) : DeviceEvent()
        class DeviceDisconnected(val device: Device) : DeviceEvent()
    }

    suspend fun initialize()

    /**
     * Remote test parsers require a temp device
     * This method should be called before reading from the [subscribe()] channel
     */
    suspend fun borrow() : Device
    suspend fun terminate()
    fun subscribe(): Channel<DeviceEvent>
}
