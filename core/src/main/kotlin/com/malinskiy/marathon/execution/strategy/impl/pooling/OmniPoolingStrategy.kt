package com.malinskiy.marathon.execution.strategy.impl.pooling

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class OmniPoolingStrategy : PoolingStrategy {
    override fun associate(device: Device): DevicePoolId = DevicePoolId("omni")
}
