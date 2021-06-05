package com.malinskiy.marathon.android.executor.listeners.filesync

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.android.adam.shell
import com.malinskiy.marathon.android.configuration.AggregationMode
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncEntry
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File

@AdbTest
class FileSyncTestRunListenerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer


    @TempDir
    lateinit var temp: File


    @ParameterizedTest
    @EnumSource(AggregationMode::class)
    fun testDefault(mode: AggregationMode) {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableListOf(
                    FileSyncEntry(
                        "screenshots",
                        mode
                    )
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val fileManager = mock<FileManager>()
        val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration
        val listener = FileSyncTestRunListener(poolId, device, androidConfiguration.fileSyncConfiguration, fileManager)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot(externalStorage = "/sdcard")
                    shell("rm -r /sdcard/screenshots", "")
                    shell("mkdir /sdcard/screenshots", "")

                    shell("ls -l /sdcard/screenshots", "")
                }
                features("emulator-5554")
            }

            device.setup()

            listener.beforeTestRun()
            listener.testRunEnded(1000, mapOf())

            when (mode) {
                AggregationMode.DEVICE_AND_POOL -> verify(fileManager, times(1)).createFolder(
                    FolderType.DEVICE_FILES,
                    poolId,
                    device.toDeviceInfo()
                )
                AggregationMode.DEVICE -> verify(fileManager, times(1)).createFolder(FolderType.DEVICE_FILES, device.toDeviceInfo())
                AggregationMode.POOL -> verify(fileManager, times(1)).createFolder(FolderType.DEVICE_FILES, poolId)
                AggregationMode.TEST_RUN -> verify(fileManager, times(1)).createFolder(FolderType.DEVICE_FILES)
            }
        }
    }
}
