package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.ddmlib.AndroidDeviceTestRunner
import com.malinskiy.marathon.android.ddmlib.DdmlibAndroidDevice
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
import com.malinskiy.marathon.android.model.AndroidTestBundle
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toClassName
import com.malinskiy.marathon.time.SystemTimer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Clock
import com.malinskiy.marathon.test.Test as MarathonTest

class AndroidDeviceTestRunnerTest {

    @Test
    fun `should handle junit Ignore annotation`() {
        verifyIgnored("org.junit.Ignore")
    }

    @Test
    fun `should handle Suppress annotation`() {
        verifyIgnored("android.support.test.filters.Suppress")
    }

    @Test
    fun `should handle suitebuilder Suppress annotation`() {
        verifyIgnored("android.test.suitebuilder.annotation.Suppress")
    }

    private fun verifyIgnored(annotationName: String) {
        val ddmsDevice = mock<IDevice>()
        val androidConfiguration = mock<VendorConfiguration.AndroidConfiguration>()
        val testBundleIdentifier = mock<AndroidTestBundleIdentifier>()
        whenever(ddmsDevice.serialNumber).doReturn("testSerial")
        whenever(ddmsDevice.version).doReturn(AndroidVersion(26))
        val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
        whenever(testBundleIdentifier.identify(MarathonTest("test", "test", "test", emptyList()))).doReturn(
            AndroidTestBundle(
                null,
                apkFile,
                null,
                null,
            )
        )
        val output = File("")
        val vendorConfiguration = VendorConfiguration.AndroidConfiguration(
            androidSdk = File(""),
            applicationOutput = File(""),
            testApplicationOutput = apkFile,
            extraApplicationsOutput = emptyList(),
            splitApks = null,
        )
        val configuration = Configuration.Builder(
            name = "",
            outputDir = output,
        ).apply {
            this.vendorConfiguration = vendorConfiguration    
            analyticsTracking = false 
        }.build()

        val device = DdmlibAndroidDevice(
            ddmsDevice,
            testBundleIdentifier,
            "testSerial",
            configuration,
            androidConfiguration,
            Track(),
            SystemTimer(Clock.systemDefaultZone()),
            SerialStrategy.AUTOMATIC
        )

        val androidDeviceTestRunner = AndroidDeviceTestRunner(device, testBundleIdentifier)

        val junitIgnoredTest =
            MarathonTest("ignored", "ignored", "ignored", listOf(MetaProperty(annotationName)))
        val identifier = junitIgnoredTest.toMarathonTestIdentifier()
        val validTest = MarathonTest("test", "test", "test", emptyList())
        val batch = TestBatch(listOf(junitIgnoredTest, validTest))
        val listener = mock<AndroidTestRunListener>()
        runBlocking {
            androidDeviceTestRunner.execute(configuration, batch, listener)
            verify(listener).beforeTestRun()
            verify(listener).testStarted(eq(identifier))
            verify(listener).testIgnored(eq(identifier))
            verify(listener).testEnded(eq(identifier), eq(hashMapOf()))
            verify(listener).afterTestRun()
        }

        verifyNoMoreInteractions(listener)
    }

    @Test
    fun `should send runEnded if only ignored tests are executed`() {
        val ddmsDevice = mock<IDevice>()
        val androidConfiguration = mock<VendorConfiguration.AndroidConfiguration>()
        val testBundleIdentifier = mock<AndroidTestBundleIdentifier>()

        whenever(ddmsDevice.serialNumber).doReturn("testSerial")
        whenever(ddmsDevice.version).doReturn(AndroidVersion(26))
        val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
        whenever(testBundleIdentifier.identify(MarathonTest("ignored", "ignored", "ignored", emptyList()))).thenReturn(
            AndroidTestBundle(
                null,
                apkFile,
                null,
                null,
            )
        )
        val output = File("")
        val configuration = Configuration.Builder(
            name = "",
            outputDir = output,
        ).apply {
            vendorConfiguration = VendorConfiguration.AndroidConfiguration(
                androidSdk = File(""),
                applicationOutput = File(""),
                testApplicationOutput = apkFile,
                extraApplicationsOutput = emptyList(),
                splitApks = null,
            )
            analyticsTracking = false 
        }.build()

        val device =
            DdmlibAndroidDevice(
                ddmsDevice,
                testBundleIdentifier,
                "testSerial",
                configuration,
                androidConfiguration,
                Track(),
                SystemTimer(Clock.systemDefaultZone()),
                SerialStrategy.AUTOMATIC
            )
        val androidDeviceTestRunner = AndroidDeviceTestRunner(device, testBundleIdentifier)


        val ignoredTest =
            MarathonTest("ignored", "ignored", "ignored", listOf(MetaProperty("org.junit.Ignore")))
        val identifier = ignoredTest.toMarathonTestIdentifier()
        val batch = TestBatch(listOf(ignoredTest))
        val listener = mock<AndroidTestRunListener>()
        runBlocking {
            androidDeviceTestRunner.execute(configuration, batch, listener)
            verify(listener).beforeTestRun()
            verify(listener).testStarted(eq(identifier))
            verify(listener).testIgnored(eq(identifier))
            verify(listener).testEnded(eq(identifier), eq(emptyMap()))
            verify(listener).testRunEnded(eq(0), eq(emptyMap()))
            verify(listener).afterTestRun()
        }
        verifyNoMoreInteractions(listener)
    }
}

private fun com.malinskiy.marathon.test.Test.toMarathonTestIdentifier() =
    TestIdentifier(toClassName(), method)
