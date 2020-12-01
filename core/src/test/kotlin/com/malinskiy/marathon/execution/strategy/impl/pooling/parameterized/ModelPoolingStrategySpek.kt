package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.DeviceStub
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ModelPoolingStrategyTest {
    val strategy = ModelPoolingStrategy()

    @Test
    fun `should return DevicePoolId with name equals to device model`() {
        val model = "TestModel"
        val device = DeviceStub(model = model)
        strategy.associate(device).name shouldBeEqualTo model
    }
}
