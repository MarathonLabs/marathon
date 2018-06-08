package com.malinskiy.marathon.execution.strategy.impl.pooling

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class PoolPerDevicePoolingStrategy : PoolingStrategy {
    override fun associate(device: Device): DevicePoolId {
        return DevicePoolId(device.serialNumber)
    }
}