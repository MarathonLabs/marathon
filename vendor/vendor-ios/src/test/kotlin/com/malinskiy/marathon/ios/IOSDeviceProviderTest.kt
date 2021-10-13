package com.malinskiy.marathon.ios

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Clock

class IOSDeviceProviderTest {
    val vendorConfiguration = VendorConfiguration.IOSConfiguration(
        derivedDataDir = File(""),
        xctestrunPath = File(""),
        remoteUsername = "testuser",
        remotePrivateKey = File("/home/fakekey"),
        knownHostsPath = null,
        remoteRsyncPath = "/remote/rsync",
        sourceRoot = File(""),
        debugSsh = false,
        alwaysEraseSimulators = true
    )
    private val configuration = Configuration(
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
        strictMode = null,
        uncompletedTestRetryQuota = null,
        testClassRegexes = null,
        includeSerialRegexes = null,
        excludeSerialRegexes = null,
        testBatchTimeoutMillis = null,
        testOutputTimeoutMillis = null,
        debug = null,
        screenRecordingPolicy = null,
        vendorConfiguration = vendorConfiguration,
        analyticsTracking = false,
        deviceInitializationTimeoutMillis = null
    )
    private val provider = IOSDeviceProvider(configuration, vendorConfiguration, Track(), SystemTimer(Clock.systemDefaultZone()))

    @Test
    fun `should close the channel`() {
        runBlocking {
            provider.terminate()
        }

        provider.subscribe().isClosedForReceive shouldBeEqualTo true
        provider.subscribe().isClosedForSend shouldBeEqualTo true
    }
}
