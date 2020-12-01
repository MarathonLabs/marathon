package com.malinskiy.marathon.device

import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

class DeviceFeatureTest {
    @Test
    fun shouldParseValidUppercaseString() {
        DeviceFeature.fromString("VIDEO").shouldEqual(DeviceFeature.VIDEO)
    }

    @Test
    fun shouldParseValidLowercaseString() {
        DeviceFeature.fromString("screenshot").shouldEqual(DeviceFeature.SCREENSHOT)
    }

    @Test
    fun shouldThrowExceptionForInvalidString() {
        { DeviceFeature.fromString("key let off") } shouldThrow RuntimeException::class
    }
}
