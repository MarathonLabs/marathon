package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.DeviceStub
import com.malinskiy.marathon.device.OperatingSystem
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class OperatingSystemVersionPoolingStrategyTest {
    val strategy = OperatingSystemVersionPoolingStrategy()

    @Test
    fun `should return DevicePoolId with name equals to device operating system version`() {
        val operatingSystemVersionName = "27"
        val device = DeviceStub(
            operatingSystem = OperatingSystem(operatingSystemVersionName)
        )
        strategy.associate(device).name shouldBeEqualTo operatingSystemVersionName
    }
}
