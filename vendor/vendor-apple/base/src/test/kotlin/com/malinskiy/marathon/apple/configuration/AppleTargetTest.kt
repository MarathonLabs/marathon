package com.malinskiy.marathon.apple.configuration

import com.malinskiy.marathon.apple.configuration.AppleTarget
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AppleTargetTest {
    @ParameterizedTest
    @CsvSource(
        "iPhone X, com.apple.CoreSimulator.SimDeviceType.iPhone-X, watchOS 3.2, com.apple.CoreSimulator.SimRuntime.watchOS-3-2"
    )
    fun testFQIDs(device: String, expectedDevice: String, runtime: String, expectedRuntime: String) {
        val profile = AppleTarget.SimulatorProfile(
            deviceTypeId = device,
            runtimeId = runtime,
        )
        profile.fullyQualifiedDeviceTypeId shouldBeEqualTo expectedDevice
        profile.fullyQualifiedRuntimeId shouldBeEqualTo expectedRuntime
    }
}
