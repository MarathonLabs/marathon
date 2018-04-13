package com.malinskiy.marathon.device

import com.malinskiy.marathon.test.Batch

interface Device {
    open val operatingSystem: OperatingSystem
    open val serialNumber: String
    open val networkState: NetworkState
    open val healthy: Boolean

    fun execute(batch: Batch)
}