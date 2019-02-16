package com.malinskiy.marathon.analytics.tracker.device

import com.malinskiy.marathon.device.Device

interface DeviceTracker {
    suspend fun trackProviderDevicePreparing(device: Device, block: suspend () -> Unit)

    suspend fun trackDevicePreparing(device: Device, block: suspend () -> Unit)
}