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
import com.malinskiy.marathon.android.adam.shell
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@AdbTest
class RemoteFileManagerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @TempDir
    lateinit var temp: File

    @Test
    fun testCreateRemoteDirectory() {
        val configuration = TestConfigurationFactory.create()
        val device = TestDeviceFactory.create(client, configuration, mock())
        val manager = RemoteFileManager(device)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot(externalStorage = "/sdcard")

                    shell("mkdir /sdcard", "")
                    shell("mkdir /sdcard/somepath", "")
                }
                features("emulator-5554")
            }

            device.setup()
            manager.createRemoteDirectory()
            manager.createRemoteDirectory("/sdcard/somepath")
        }
    }

    @Test
    fun testRemoveRemoteDirectory() {
        val configuration = TestConfigurationFactory.create()
        val device = TestDeviceFactory.create(client, configuration, mock())
        val manager = RemoteFileManager(device)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot(externalStorage = "/sdcard")

                    shell("rm -r /sdcard", "")
                }
                features("emulator-5554")
            }

            device.setup()
            manager.removeRemoteDirectory()
        }
    }

    @Test
    fun testRemoveRemotePath() {
        val configuration = TestConfigurationFactory.create()
        val device = TestDeviceFactory.create(client, configuration, mock())
        val manager = RemoteFileManager(device)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()

                    shell("rm /sdcard/something", "")
                    shell("rm -r /sdcard/something2", "")
                }
                features("emulator-5554")
            }

            device.setup()
            manager.removeRemotePath(remotePath = "/sdcard/something")
            manager.removeRemotePath(remotePath = "/sdcard/something2", recursive = true)
        }
    }

    @Test
    fun testRemoteVideo() {
        val configuration = TestConfigurationFactory.create()
        val device = TestDeviceFactory.create(client, configuration, mock())
        val manager = RemoteFileManager(device)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }
            device.setup()

            val actual = manager.remoteVideoForTest(com.malinskiy.marathon.test.Test("pkg", "clazz", "method", emptyList()), "batch-id")
            assertThat(actual).isEqualTo("/sdcard/pkg.clazz-method-batch-id.mp4")
        }
    }
}
