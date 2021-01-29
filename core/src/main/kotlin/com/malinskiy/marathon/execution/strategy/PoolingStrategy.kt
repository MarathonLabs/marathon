package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId

sealed class PoolingStrategy {
    abstract fun associate(device: Device): DevicePoolId
}


class OmniPoolingStrategy : PoolingStrategy() {
    override fun associate(device: Device): DevicePoolId = DevicePoolId("omni")

    override fun hashCode() = javaClass.canonicalName.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }

    override fun toString(): String {
        return "OmniPoolingStrategy()"
    }
}


class AbiPoolingStrategy : PoolingStrategy() {
    override fun associate(device: Device): DevicePoolId = DevicePoolId(device.abi)

    override fun hashCode() = javaClass.canonicalName.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }

    override fun toString(): String {
        return "AbiPoolingStrategy()"
    }
}

class ComboPoolingStrategy(private val list: List<PoolingStrategy>) : PoolingStrategy() {
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

class ManufacturerPoolingStrategy : PoolingStrategy() {
    override fun associate(device: Device) = DevicePoolId(device.manufacturer)

    override fun hashCode() = javaClass.canonicalName.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }

    override fun toString(): String {
        return "ManufacturerPoolingStrategy()"
    }


}

class ModelPoolingStrategy : PoolingStrategy() {
    override fun associate(device: Device) = DevicePoolId(device.model)

    override fun hashCode() = javaClass.canonicalName.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }

    override fun toString(): String {
        return "ModelPoolingStrategy()"
    }
}

class OperatingSystemVersionPoolingStrategy : PoolingStrategy() {
    override fun associate(device: Device) = DevicePoolId(device.operatingSystem.version)

    override fun hashCode() = javaClass.canonicalName.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }

    override fun toString(): String {
        return "OperatingSystemVersionPoolingStrategy()"
    }
}
