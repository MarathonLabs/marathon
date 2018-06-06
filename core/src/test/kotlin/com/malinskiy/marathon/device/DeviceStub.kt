package com.malinskiy.marathon.device

import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.TestBatch

class DeviceStub(override var operatingSystem: OperatingSystem,
                 override var serialNumber: String,
                 override var networkState: NetworkState,
                 override var healthy: Boolean) : Device {
    override val model: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val manufacturer: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val deviceFeatures: Collection<DeviceFeature>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override suspend fun execute(configuration: Configuration, devicePoolId: DevicePoolId, testBatch: TestBatch, tracker: Analytics) {
    }

    override suspend fun prepare(configuration: Configuration) {
    }
}
