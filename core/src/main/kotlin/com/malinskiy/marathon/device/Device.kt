package com.malinskiy.marathon.device

import com.malinskiy.marathon.analytics.Tracker
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.TestBatch

interface Device {
    val operatingSystem: OperatingSystem
    val serialNumber: String
    val model: String
    val manufacturer: String
    val networkState: NetworkState
    val deviceFeatures: Collection<DeviceFeature>
    val healthy: Boolean

    suspend fun execute(configuration: Configuration,
                        devicePoolId: DevicePoolId,
                        testBatch: TestBatch,
                        tracker: Tracker)

    suspend fun prepare(configuration: Configuration)
}

