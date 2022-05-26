package com.malinskiy.marathon.android.executor.listeners.filesync

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.android.adam.pushFile
import com.malinskiy.marathon.android.adam.shell
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AggregationMode
import com.malinskiy.marathon.config.vendor.android.FilePushEntry
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncEntry
import com.malinskiy.marathon.config.vendor.android.PathRoot
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File
import kotlin.io.path.createTempDirectory

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
                mutableSetOf(
                    FileSyncEntry("screenshots", aggregationMode = mode)
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val fileManager = mock<FileManager>()
        val androidConfiguration = configuration.vendorConfiguration as VendorConfiguration.AndroidConfiguration
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
            listener.afterTestRun()

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

    @Test
    fun testPush() {
        val tempDir = createTempDirectory().toFile()
        val tempFile1 = File(tempDir, "fixture1").apply {
            writeText("cafebabe")
        }
        val tempFile2 = File(tempDir, "fixture2").apply {
            writeText("cafebabe1")
        }
        val tempFile3 = File(tempDir, "fixture3").apply {
            writeText("cafebabe2")
        }
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                push = mutableSetOf(
                    FilePushEntry(tempFile1.absolutePath),
                    FilePushEntry(tempFile2.absolutePath, PathRoot.LOCAL_TMP),
                    FilePushEntry(tempFile3.absolutePath, PathRoot.EXTERNAL_STORAGE),
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val fileManager = mock<FileManager>()
        val androidConfiguration = configuration.vendorConfiguration as VendorConfiguration.AndroidConfiguration
        val listener = FileSyncTestRunListener(poolId, device, androidConfiguration.fileSyncConfiguration, fileManager)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot(externalStorage = "/sdcard")
                    
                    pushFile(tempDir, "/data/local/tmp/fixture1", "511")
                    shell("md5 /data/local/tmp/fixture1", "21a270d5f59c9b05813a72bb41707266")
                    pushFile(tempDir, "/data/local/tmp/fixture2", "511")
                    shell("md5 /data/local/tmp/fixture2", "0bc54dd1e073e6b14cf526be06d86cec")
                    pushFile(tempDir, "/sdcard/fixture3", "511")
                    shell("md5 /sdcard/fixture3", "322fb8affd8a92a7d9ca5c6de32d4a41")
                    
                    shell("rm -r /data/local/tmp/fixture1", "")
                    shell("rm -r /data/local/tmp/fixture2", "")                    
                    shell("rm -r /sdcard/fixture3", "")
                }
                features("emulator-5554")
            }

            device.setup()

            listener.beforeTestRun()
            listener.afterTestRun()
        }
    }

    @Test
    fun testPull() {
        val tempDir = createTempDirectory().toFile()
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                pull = mutableSetOf(
                    FileSyncEntry("fixture1"),
                    FileSyncEntry("fixture2", PathRoot.EXTERNAL_STORAGE),
                    FileSyncEntry("fixture3", PathRoot.LOCAL_TMP),
                    FileSyncEntry("fixture4", PathRoot.APP_DATA),
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val fileManager = mock<FileManager>()
        val androidConfiguration = configuration.vendorConfiguration as VendorConfiguration.AndroidConfiguration
        val listener = FileSyncTestRunListener(poolId, device, androidConfiguration.fileSyncConfiguration, fileManager)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot(externalStorage = "/sdcard")

                    shell("rm -r /sdcard/fixture1", "")
                    shell("mkdir /sdcard/fixture1", "")

                    shell("rm -r /sdcard/fixture2", "")
                    shell("mkdir /sdcard/fixture2", "")

                    shell("rm -r /data/local/tmp/fixture3", "")
                    shell("mkdir /data/local/tmp/fixture3", "")

                    shell("rm -r /data/local/tmp/fixture4", "")
                    shell("mkdir /data/local/tmp/fixture4", "")
                    shell("run-as com.example rm -R /data/data/com.example/fixture4", "")
                    shell("run-as com.example mkdir /data/data/com.example/fixture4","")
                    
                    shell("ls -l /sdcard/fixture1", "")
                    shell("ls -l /sdcard/fixture2", "")                    
                    shell("ls -l /data/local/tmp/fixture3", "")                    
                    
                    shell("mkdir -p /data/local/tmp/fixture4 && run-as com.example sh -c 'cd /data/data/com.example/fixture4 && tar cf - .' | tar xvf - -C /data/local/tmp/fixture4 && run-as com.example rm -R /data/data/com.example/fixture4", "")
                    shell("ls -l /data/local/tmp/fixture4", "")
                }
                features("emulator-5554")
            }

            device.setup()

            val instrumentationInfo = InstrumentationInfo("com.example", "com.example.test", "androidx.test.runner.AndroidJUnitRunner")
            listener.beforeTestRun(instrumentationInfo)
            listener.afterTestRun()
        }
    }
}
