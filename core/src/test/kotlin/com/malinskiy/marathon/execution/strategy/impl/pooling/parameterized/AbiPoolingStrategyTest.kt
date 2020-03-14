package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.DeviceStub
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AbiPoolingStrategyTest {
    val strategy = AbiPoolingStrategy()

    @Test
    fun `should return DevicePoolId with name equals to device_abi`() {
        val abi = "Test_ABI"
        val device = DeviceStub(abi = abi)
        strategy.associate(device).name shouldBeEqualTo abi
    }
}
