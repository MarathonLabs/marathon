package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.android.executor.AndroidDeviceTestRunner
import com.malinskiy.marathon.android.executor.toTestIdentifier
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File

class AndroidDeviceTestRunnerSpek : Spek({
    describe("AndroidDeviceTestRunner") {
        it("should handle ignored tests before execution") {
            val ddmsDevice = mock<IDevice>()
            whenever(ddmsDevice.serialNumber).doReturn("testSerial")
            whenever(ddmsDevice.version).doReturn(AndroidVersion(26))
            val device = AndroidDevice(ddmsDevice)
            val androidDeviceTestRunner = AndroidDeviceTestRunner(device)
            val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
            val output = File("")
            val configuration = Configuration(name = "",
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
                    vendorConfiguration = AndroidConfiguration(
                            File(""),
                            applicationOutput = File(""),
                            testApplicationOutput = apkFile
                    ),
                    analyticsTracking = false
            )
            val ignoredTest = Test("ignored", "ignored", "ignored", listOf(MetaProperty("org.junit.Ignore")))
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
    }
})