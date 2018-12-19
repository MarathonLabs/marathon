package com.malinskiy.marathon.ios

interface HealthChangeListener {
    suspend fun onDisconnect(device: IOSDevice)
}
