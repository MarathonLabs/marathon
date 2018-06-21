package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class ModelPoolingStrategy : PoolingStrategy {
    override fun associate(device: Device) = DevicePoolId(device.model)
}
