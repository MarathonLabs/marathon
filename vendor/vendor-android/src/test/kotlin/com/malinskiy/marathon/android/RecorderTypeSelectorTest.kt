package com.malinskiy.marathon.android

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.malinskiy.marathon.config.vendor.android.RecorderType
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.device.DeviceFeature
import org.junit.jupiter.api.Test

class RecorderTypeSelectorTest {
    @Test
    fun testDefault() {
        assertThat(
            RecorderTypeSelector.selectRecorderType(listOf(DeviceFeature.SCREENSHOT, DeviceFeature.VIDEO), ScreenRecordConfiguration())
        ).isEqualTo(DeviceFeature.VIDEO)
    }

    @Test
    fun testPreferredScreenshot() {
        assertThat(
            RecorderTypeSelector.selectRecorderType(
                listOf(DeviceFeature.SCREENSHOT, DeviceFeature.VIDEO),
                ScreenRecordConfiguration(preferableRecorderType = RecorderType.SCREENSHOT)
            )
        ).isEqualTo(DeviceFeature.SCREENSHOT)
    }

    @Test
    fun testPreferredVideo() {
        assertThat(
            RecorderTypeSelector.selectRecorderType(
                listOf(DeviceFeature.SCREENSHOT, DeviceFeature.VIDEO),
                ScreenRecordConfiguration(preferableRecorderType = RecorderType.VIDEO)
            )
        ).isEqualTo(DeviceFeature.VIDEO)
    }

    @Test
    fun testPreferredVideoButUnavailable() {
        assertThat(
            RecorderTypeSelector.selectRecorderType(
                listOf(DeviceFeature.SCREENSHOT),
                ScreenRecordConfiguration(preferableRecorderType = RecorderType.VIDEO)
            )
        ).isEqualTo(DeviceFeature.SCREENSHOT)
    }

    @Test
    fun testNoneAvailable() {
        assertThat(
            RecorderTypeSelector.selectRecorderType(emptyList(), ScreenRecordConfiguration())
        ).isNull()
    }
}
