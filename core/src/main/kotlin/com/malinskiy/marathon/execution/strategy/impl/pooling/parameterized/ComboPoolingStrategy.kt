package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class ComboPoolingStrategy(private val list: List<PoolingStrategy>) : PoolingStrategy {
    override fun associate(device: Device): DevicePoolId {
        val acc = list.fold(StringBuilder()) { acc, strategy ->
            if (acc.isNotEmpty()) {
                acc.append('_')
            }
            acc.append(strategy.associate(device).name)
        }
        return DevicePoolId(acc.toString())
    }
}