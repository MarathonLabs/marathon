package com.malinskiy.marathon.android

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.android.extension.toScreenRecorderCommand
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncEntry
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock

@AdbTest
class VideoConfigurationTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

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

    @ParameterizedTest
    @MethodSource("apiLevels")
    fun testLongVideoDependsOnApiLevel(sdkLevel: Int, expectedTimeLimit: Int, featureIsEnabled: Boolean) {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry(
                        "screenshots"
                    )
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot(sdk = sdkLevel)
                }
                features("emulator-5554")
            }

            device.setup()
        }
        assertThat(VideoConfiguration(timeLimit = 200, increasedTimeLimitFeatureEnabled = featureIsEnabled).toScreenRecorderCommand("/sdcard/video.mp4", device))
            .isEqualTo("screenrecord --size 720x1280 --bit-rate 1000000 --time-limit $expectedTimeLimit /sdcard/video.mp4")
    }

    companion object {
        @JvmStatic
        fun apiLevels() = listOf(
            Arguments.of(33, 180, true),
            Arguments.of(34, 200, true),
            Arguments.of(33, 180, false),
            Arguments.of(34, 180, false)
        )
    }
}
