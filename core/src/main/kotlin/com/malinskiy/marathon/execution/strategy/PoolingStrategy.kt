package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.device.DevicePool

interface PoolingStrategy {

    fun createPools(devices: List<Device>): Collection<DevicePool>
}