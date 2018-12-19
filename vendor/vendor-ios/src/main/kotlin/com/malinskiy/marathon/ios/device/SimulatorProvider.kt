package com.malinskiy.marathon.ios.device

interface SimulatorProvider {
    suspend fun start()
    suspend fun stop()
}
