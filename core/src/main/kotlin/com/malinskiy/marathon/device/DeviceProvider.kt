package com.malinskiy.marathon.device

import kotlinx.coroutines.channels.Channel

interface DeviceProvider {
    sealed class DeviceEvent {
        class DeviceConnected(val device: Device) : DeviceEvent()
        class DeviceDisconnected(val device: Device) : DeviceEvent()
    }

    /**
     * Informational for scheduler, device providers should not impose timeouts internally
     */
    val deviceInitializationTimeoutMillis: Long
    suspend fun initialize()
    suspend fun terminate()
    fun subscribe(): Channel<DeviceEvent>
}
