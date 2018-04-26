package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId

interface PoolingStrategy {
    fun associate(device: Device): DevicePoolId
}