package com.malinskiy.marathon.android

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.android.adam.installApk
import com.malinskiy.marathon.android.adam.shell
import com.malinskiy.marathon.android.adam.shellFail
import com.malinskiy.marathon.config.vendor.android.AggregationMode
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncEntry
import com.malinskiy.marathon.config.vendor.android.PathRoot
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

@AdbTest
class AndroidAppInstallerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @TempDir
    lateinit var temp: File

    @Test
    fun testCleanInstallation() {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry(
                        relativePath = "screenshots",
                        aggregationMode = AggregationMode.DEVICE,
                        pathRoot = PathRoot.EXTERNAL_STORAGE,
                    )
                )
            )
        )
        val installer = AndroidAppInstaller(configuration)
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()

                    shell("pm list packages", "")
                    installApk(temp, "/data/local/tmp/app-debug.apk", "511", "122fc3b5d69b262db9b84dfc00e8f1d4", "-r")

                    shell("pm list packages", "package:com.example")
                    installApk(temp, "/data/local/tmp/app-debug-androidTest.apk", "511", "8d103498247b3711817a9f18624dede7", "-r")

                }
                features("emulator-5554")
            }

            device.setup()
            installer.prepareInstallation(device)
        }
    }

    @Test
    fun testCleanInstallationWithAutograntPermissions() {
        val configuration = TestConfigurationFactory.create(
            autoGrantPermission = true,
            fileSyncConfiguration = FileSyncConfiguration(mutableSetOf(FileSyncEntry("screenshots")))
        )
        val installer = AndroidAppInstaller(configuration)
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()

                    shell("pm list packages", "")
                    installApk(temp, "/data/local/tmp/app-debug.apk", "511", "122fc3b5d69b262db9b84dfc00e8f1d4", "-r -g")

                    shell("pm list packages", "package:com.example")
                    installApk(temp, "/data/local/tmp/app-debug-androidTest.apk", "511", "8d103498247b3711817a9f18624dede7", "-r -g")

                }
                features("emulator-5554")
            }

            device.setup()
            installer.prepareInstallation(device)
        }
    }

    @Test
    fun testReinstallWithExtraArguments() {
        val configuration = TestConfigurationFactory.create(
            installOptions = "-custom",
            fileSyncConfiguration = FileSyncConfiguration(mutableSetOf(FileSyncEntry("screenshots")))
        )
        val installer = AndroidAppInstaller(configuration)
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()

                    shell("pm list packages", "package:com.example")
                    shell("pm uninstall com.example", "Great success")
                    installApk(temp, "/data/local/tmp/app-debug.apk", "511", "122fc3b5d69b262db9b84dfc00e8f1d4", "-r -custom")

                    shell("pm list packages", "package:com.example.test")
                    shell("pm uninstall com.example.test", "Great success")
                    installApk(temp, "/data/local/tmp/app-debug-androidTest.apk", "511", "8d103498247b3711817a9f18624dede7", "-r -custom")

                }
                features("emulator-5554")
            }

            device.setup()
            installer.prepareInstallation(device)
        }
    }

    @Test
    fun testInstallException() {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry(
                        "screenshots",
                    )
                )
            )
        )
        val installer = AndroidAppInstaller(configuration)
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()

                    shell("pm list packages", "")
                    installApk(temp, "/data/local/tmp/app-debug.apk", "511", "122fc3b5d69b262db9b84dfc00e8f1d4", "-r", stdout = "Failure")

                    shell("pm list packages", "")
                    installApk(temp, "/data/local/tmp/app-debug.apk", "511", "122fc3b5d69b262db9b84dfc00e8f1d4", "-r", stdout = "Failure")

                    shell("pm list packages", "")
                    installApk(temp, "/data/local/tmp/app-debug.apk", "511", "122fc3b5d69b262db9b84dfc00e8f1d4", "-r", stdout = "Failure")
                }
                features("emulator-5554")
            }

            device.setup()

            assertThrows<DeviceSetupException> { installer.prepareInstallation(device) }
        }
    }

    @Test
    fun testPmListException() {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry(
                        "screenshots"
                    )
                )
            )
        )
        val installer = AndroidAppInstaller(configuration)
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()

                    shellFail()
                    shellFail()
                    shellFail()
                }
                features("emulator-5554")
            }

            device.setup()

            assertThrows<DeviceSetupException> { installer.prepareInstallation(device) }
        }
    }

    @Test
    fun testInstallationWithSpecialCharactersInPath() {
        val apk = File(javaClass.classLoader.getResource("apk/app-debug.apk").file).copyTo(File(temp, "().apk"))
        val testApk = File(javaClass.classLoader.getResource("apk/app-debug-androidTest.apk").file).copyTo(File(temp, "()-androidTest.apk"))

        val configuration = TestConfigurationFactory.create(
            applicationOutput = apk,
            testApplicationOutput = testApk,
        )
        val installer = AndroidAppInstaller(configuration)
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()

                    shell("pm list packages", "")
                    installApk(temp, "/data/local/tmp/--.apk", "511", "122fc3b5d69b262db9b84dfc00e8f1d4", "-r")

                    shell("pm list packages", "package:com.example")
                    installApk(temp, "/data/local/tmp/---androidTest.apk", "511", "8d103498247b3711817a9f18624dede7", "-r")

                }
                features("emulator-5554")
            }

            device.setup()
            installer.prepareInstallation(device)
        }
    }
}
