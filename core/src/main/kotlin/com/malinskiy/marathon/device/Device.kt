package com.malinskiy.marathon.device

import com.malinskiy.marathon.test.TestBatch

interface Device {
    open val operatingSystem: OperatingSystem
    open val serialNumber: String
    open val model: String
    open val manufacturer: String
    open val networkState: NetworkState
    open val deviceFeatures: Collection<DeviceFeature>
    open val healthy: Boolean

    fun execute(testBatch: TestBatch)
}

