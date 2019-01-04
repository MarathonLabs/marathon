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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComboPoolingStrategy

        if (list != other.list) return false

        return true
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }

    override fun toString(): String {
        return "ComboPoolingStrategy(list=$list)"
    }


}
