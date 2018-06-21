package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.DeviceStub
import org.amshove.kluent.shouldBeEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class AbiPoolingStrategySpek : Spek({
    describe("pooling strategy based on device ABI tests") {
        val strategy by memoized { AbiPoolingStrategy() }
        it("should return DevicePoolId with name equals to device.abi") {
            val abi = "Test_ABI"
            val device = DeviceStub(abi = abi)
            strategy.associate(device).name shouldBeEqualTo abi
        }
    }
})