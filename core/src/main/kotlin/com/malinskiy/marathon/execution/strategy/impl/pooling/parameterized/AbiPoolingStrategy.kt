package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class AbiPoolingStrategy : PoolingStrategy {
    override fun associate(device: Device): DevicePoolId = DevicePoolId(device.abi)

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }
}
