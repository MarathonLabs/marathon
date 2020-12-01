package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.DeviceStub
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ManufacturerPoolingStrategyTest {
    val strategy = ManufacturerPoolingStrategy()

    @Test
    fun `should return DevicePoolId with name equals to device manufacturer`() {
        val deviceManufacturer = "TestDeviceManufacturer"
        val device = DeviceStub(manufacturer = deviceManufacturer)
        strategy.associate(device).name shouldBeEqualTo deviceManufacturer
    }
}
