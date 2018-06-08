package com.malinskiy.marathon.device

import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.TestBatch

class DeviceStub(override val operatingSystem: OperatingSystem = OperatingSystem("23"),
                 override val serialNumber: String = "serial",
                 override val networkState: NetworkState = NetworkState.CONNECTED,
                 override val healthy: Boolean = true,
                 override val model: String = "model",
                 override val manufacturer: String = "manufacturer",
                 override val deviceFeatures: Collection<DeviceFeature> = emptyList()) : Device {
    override suspend fun execute(configuration: Configuration, devicePoolId: DevicePoolId, testBatch: TestBatch, tracker: Analytics) {
    }

    override suspend fun prepare(configuration: Configuration) {
    }
}
