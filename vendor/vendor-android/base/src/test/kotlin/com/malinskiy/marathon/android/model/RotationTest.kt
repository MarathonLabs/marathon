package com.malinskiy.marathon.android.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.marathon.device.screenshot.Rotation
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class RotationTest {
    @ParameterizedTest
    @EnumSource(Rotation::class)
    fun testValue(rotation: Rotation) {
        assertThat(rotation.value).isEqualTo(
            when (rotation) {
                Rotation.ROTATION_0 -> 0
                Rotation.ROTATION_180 -> 2
                Rotation.ROTATION_270 -> 3
                Rotation.ROTATION_90 -> 1
            }
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4])
    fun testOf(value: Int) {
        assertThat(Rotation.of(value)).isEqualTo(
            when (value) {
                0 -> Rotation.ROTATION_0
                1 -> Rotation.ROTATION_90
                2 -> Rotation.ROTATION_180
                3 -> Rotation.ROTATION_270
                else -> null
            }
        )
    }
}

