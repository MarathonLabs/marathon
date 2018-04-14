package com.malinskiy.marathon.execution.strategy.impl

import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class OmniPoolingStrategy : PoolingStrategy {
    override fun createPools(deviceProvider: DeviceProvider): Collection<DevicePool> {
        val devices = deviceProvider.getDevices()
        return listOf(DevicePool(devices = devices))
    }
}