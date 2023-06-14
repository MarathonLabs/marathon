package com.malinskiy.marathon.android

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.marathon.android.extension.toScreenRecorderCommand
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import org.junit.jupiter.api.Test

class VideoConfigurationTest {
    @Test
    fun testDefaults() {
        assertThat(VideoConfiguration().toScreenRecorderCommand("/sdcard/video.mp4"))
            .isEqualTo("screenrecord --size 720x1280 --bit-rate 1000000 --time-limit 180 /sdcard/video.mp4")
    }

    @Test
    fun testLongVideo() {
        assertThat(VideoConfiguration(timeLimit = 200).toScreenRecorderCommand("/sdcard/video.mp4"))
            .isEqualTo("screenrecord --size 720x1280 --bit-rate 1000000 --time-limit 180 /sdcard/video.mp4")
    }
}
