package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.configuration.AndroidConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.ddmlib.AndroidDeviceTestRunner
import com.malinskiy.marathon.android.ddmlib.DdmlibAndroidDevice
import com.malinskiy.marathon.android.ddmlib.toTestIdentifier
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.spek.initKoin
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.SystemTimer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import ddmlibModule
import org.amshove.kluent.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.time.Clock

class AndroidDeviceTestRunnerSpek : Spek(
    {
        initKoin()

        describe("AndroidDeviceTestRunner") {
            it("should handle ignored tests before execution") {
                val ddmsDevice = mock<IDevice>()
                whenever(ddmsDevice.serialNumber).doReturn("testSerial")
                whenever(ddmsDevice.version).doReturn(AndroidVersion(26))
                val device = DdmlibAndroidDevice(ddmsDevice, Track(), SystemTimer(Clock.systemDefaultZone()), SerialStrategy.AUTOMATIC)
                val androidDeviceTestRunner = AndroidDeviceTestRunner(device)
                val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
                val output = File("")
                val configuration = Configuration(
                    name = "",
                    outputDir = output,
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
                    strictMode = null,
                    uncompletedTestRetryQuota = null,
                    testClassRegexes = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testBatchTimeoutMillis = null,
                    testOutputTimeoutMillis = null,
                    debug = null,
                    screenRecordingPolicy = null,
                    vendorConfiguration = AndroidConfiguration(
                        File(""),
                        applicationOutput = File(""),
                        testApplicationOutput = apkFile,
                        implementationModules = listOf(ddmlibModule)
                    ),
                    analyticsTracking = false
                )
                val ignoredTest =
                    Test("ignored", "ignored", "ignored", listOf(MetaProperty("org.junit.Ignore")))
                val identifier = ignoredTest.toTestIdentifier()
                val validTest = Test("test", "test", "test", emptyList())
                val batch = TestBatch(listOf(ignoredTest, validTest))
                val listener = mock<ITestRunListener>()
                androidDeviceTestRunner.execute(configuration, batch, listener)
                verify(listener).testStarted(eq(identifier))
                verify(listener).testIgnored(eq(identifier))
                verify(listener).testEnded(eq(identifier), eq(hashMapOf()))
                verifyNoMoreInteractions(listener)

            }
            it("should send runEnded if only ignored tests are executed") {
                val ddmsDevice = mock<IDevice>()
                whenever(ddmsDevice.serialNumber).doReturn("testSerial")
                whenever(ddmsDevice.version).doReturn(AndroidVersion(26))
                val device = DdmlibAndroidDevice(ddmsDevice, Track(), SystemTimer(Clock.systemDefaultZone()), SerialStrategy.AUTOMATIC)
                val androidDeviceTestRunner = AndroidDeviceTestRunner(device)
                val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
                val output = File("")
                val configuration = Configuration(
                    name = "",
                    outputDir = output,
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
                    strictMode = null,
                    uncompletedTestRetryQuota = null,
                    testClassRegexes = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testBatchTimeoutMillis = null,
                    testOutputTimeoutMillis = null,
                    debug = null,
                    screenRecordingPolicy = null,
                    vendorConfiguration = AndroidConfiguration(
                        File(""),
                        applicationOutput = File(""),
                        testApplicationOutput = apkFile,
                        implementationModules = listOf(ddmlibModule)
                    ),
                    analyticsTracking = false
                )
                val ignoredTest =
                    Test("ignored", "ignored", "ignored", listOf(MetaProperty("org.junit.Ignore")))
                val identifier = ignoredTest.toTestIdentifier()
                val batch = TestBatch(listOf(ignoredTest))
                val listener = mock<ITestRunListener>()
                androidDeviceTestRunner.execute(configuration, batch, listener)
                verify(listener).testStarted(eq(identifier))
                verify(listener).testIgnored(eq(identifier))
                verify(listener).testEnded(eq(identifier), eq(emptyMap()))
                verify(listener).testRunEnded(eq(0), eq(emptyMap()))
                verifyNoMoreInteractions(listener)
            }
        }
    })
