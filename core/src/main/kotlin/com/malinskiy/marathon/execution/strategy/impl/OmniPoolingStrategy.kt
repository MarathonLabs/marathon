package com.malinskiy.marathon.execution.strategy.impl

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class OmniPoolingStrategy : PoolingStrategy {
    override fun createPools(devices: List<Device>): Collection<DevicePool> {
        return listOf(DevicePool(name = "omni", devices = devices))
    }
}