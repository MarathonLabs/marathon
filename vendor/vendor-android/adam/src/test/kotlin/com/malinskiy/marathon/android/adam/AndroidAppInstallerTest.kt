package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.adam.server.stub.dsl.DeviceExpectation
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.time.SystemTimer
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Clock

@AdbTest
class AndroidAppInstallerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @Test
    fun testCleanInstallation() {
        val configuration = createConfiguration(false)
        val installer = AndroidAppInstaller(configuration)
        val logcatManager: LogcatManager = mock()
        val device = AdamAndroidDevice(
            client = client,
            deviceStateTracker = DeviceStateTracker(),
            logcatManager = logcatManager,
            "emulator-5554",
            configuration,
            configuration.vendorConfiguration as AndroidConfiguration,
            Track(),
            SystemTimer(Clock.systemDefaultZone()),
            SerialStrategy.AUTOMATIC
        )

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    shell(
                        "getprop", """
                        [sys.boot_completed]: [1]
                        [ro.product.cpu.abi]: [x86]
                        [ro.build.version.sdk]: [27]
                        [ro.build.version.codename], [REL]
                        [ro.product.model]: [Android SDK built for x86]
                        [ro.product.manufacturer]: [Google] 
                        [ro.boot.serialno]: [EMULATOR30X6X5X0]
                        
                        x0
                    """.trimIndent()
                    )
                    shell("dumpsys input", "SurfaceOrientation: 0")
                    shell("echo \$EXTERNAL_STORAGE", "/sdcard\n")
                    shell("ls /system/bin/screenrecord", "/system/bin/screenrecord")
                    shell("ls /system/bin/md5", "/system/bin/md5")
                    shell(
                        "getprop", """
                        [sys.boot_completed]: [1]
                        [ro.product.cpu.abi]: [x86]
                        [ro.build.version.sdk]: [27]
                        [ro.build.version.codename], [REL]
                        [ro.product.model]: [Android SDK built for x86]
                        [ro.product.manufacturer]: [Google] 
                        [ro.boot.serialno]: [EMULATOR30X6X5X0]
                        
                        x0
                    """.trimIndent()
                    )

                    shell("pm list packages", "")
                    session {
                        respondOkay()
                        expectCmd { "sync:" }.accept()
                        expectSend { "/data/local/tmp/app-debug.apk,511" }
                            .receiveFile(File.createTempFile("xyinya", "y "))
                            .done()
                    }
                    shell("md5 /data/local/tmp/app-debug.apk", "122fc3b5d69b262db9b84dfc00e8f1d4")
                    shell("pm install -r -r   /data/local/tmp/app-debug.apk", "Success")
                    shell("rm /data/local/tmp/app-debug.apk", "")

                    shell("pm list packages", "package:com.example")
                    session {
                        respondOkay()
                        expectCmd { "sync:" }.accept()
                        expectSend { "/data/local/tmp/app-debug-androidTest.apk,511" }
                            .receiveFile(File.createTempFile("xyinya", "y "))
                            .done()
                    }
                    shell("md5 /data/local/tmp/app-debug-androidTest.apk", "8d103498247b3711817a9f18624dede7")
                    shell("pm install -r -r   /data/local/tmp/app-debug-androidTest.apk", "Success")
                    shell("rm /data/local/tmp/app-debug-androidTest.apk", "")

                    other { transportCmd ->
                        when (transportCmd) {
                            "host-serial:emulator-5554:features" -> {
                                output.respondOkay()
                                output.respondStringV1("cmd")
                            }
                        }
                        true
                    }
                }
            }

            device.setup()
            installer.prepareInstallation(device)
        }
    }

    fun DeviceExpectation.property(name: String, value: String) = shell("getprop $name", value)
    fun DeviceExpectation.shell(cmd: String, stdout: String) {
        session {
            respondOkay()
            expectShell { "$cmd;echo x$?" }.accept().respond("${stdout}x0")
        }
    }

    private fun createConfiguration(strictMode: Boolean): Configuration {
        return Configuration(
            name = "",
            outputDir = File(""),
            analyticsConfiguration = null,
            poolingStrategy = null,
            shardingStrategy = null,
            sortingStrategy = null,
            batchingStrategy = null,
            flakinessStrategy = null,
            retryStrategy = null,
            filteringConfiguration = null,
            ignoreFailures = null,
            isCodeCoverageEnabled = null,
            fallbackToScreenshots = null,
            strictMode = strictMode,
            uncompletedTestRetryQuota = null,
            testClassRegexes = null,
            includeSerialRegexes = null,
            excludeSerialRegexes = null,
            testBatchTimeoutMillis = null,
            testOutputTimeoutMillis = null,
            debug = false,
            screenRecordingPolicy = null,
            vendorConfiguration = AndroidConfiguration(
                androidSdk = File(""),
                applicationOutput = File(javaClass.classLoader.getResource("apk/app-debug.apk").file),
                testApplicationOutput = File(javaClass.classLoader.getResource("apk/app-debug-androidTest.apk").file),
                implementationModules = listOf(adamModule),
            ),
            analyticsTracking = false,
            deviceInitializationTimeoutMillis = null
        )
    }
}
