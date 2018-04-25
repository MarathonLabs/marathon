package com.malinskiy.marathon.execution.strategy.impl.pooling

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class PoolPerDeviceStrategy : PoolingStrategy {
    override fun createPools(devices: List<Device>): Collection<DevicePool> = devices.map {
        DevicePool(name = it.serialNumber, devices = listOf(it))
    }
}
