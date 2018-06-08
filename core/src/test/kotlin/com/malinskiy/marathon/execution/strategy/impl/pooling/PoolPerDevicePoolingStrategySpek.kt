package com.malinskiy.marathon.execution.strategy.impl.pooling

import com.malinskiy.marathon.device.DeviceStub
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class PoolPerDevicePoolingStrategySpek : Spek({
    describe("pool per deice pooling stategy test") {
        it("should return same DevicePoolId for same device") {
            val device = DeviceStub(serialNumber = "serial")
            val strategy = PoolPerDevicePoolingStrategy()
            strategy.associate(device) shouldEqual strategy.associate(device)
        }
        it("should return different DevicePoolIds for device with different serials") {
            val device = DeviceStub(serialNumber = "serial1")
            val device2 = DeviceStub(serialNumber = "serial2")
            val strategy = PoolPerDevicePoolingStrategy()
            strategy.associate(device) shouldNotEqual strategy.associate(device2)
        }
    }
})