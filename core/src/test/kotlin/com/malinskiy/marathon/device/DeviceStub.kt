package com.malinskiy.marathon.device

import com.malinskiy.marathon.test.Batch

class DeviceStub(override var operatingSystem: OperatingSystem,
                 override var serialNumber: String,
                 override var networkState: NetworkState,
                 override var healthy: Boolean) : Device {

    override fun execute(batch: Batch) {
        TODO("not implemented")
    }
}