package com.malinskiy.marathon.ios

interface HealthListener {
    suspend fun onDisconnect(device: IOSDevice)
}
