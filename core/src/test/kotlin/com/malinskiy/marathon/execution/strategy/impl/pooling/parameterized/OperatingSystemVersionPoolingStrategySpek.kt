package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.DeviceStub
import com.malinskiy.marathon.device.OperatingSystem
import org.amshove.kluent.shouldBeEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class OperatingSystemVersionPoolingStrategySpek : Spek({
    describe("pooling strategy based on sdk version tests") {
        val strategy by memoized { OperatingSystemVersionPoolingStrategy() }
        it("should return DevicePoolId with name equals to device operating system version") {
            val operatingSystemVersionName = "27"
            val device = DeviceStub(operatingSystem = OperatingSystem(operatingSystemVersionName))
            strategy.associate(device).name shouldBeEqualTo operatingSystemVersionName
        }
    }
})