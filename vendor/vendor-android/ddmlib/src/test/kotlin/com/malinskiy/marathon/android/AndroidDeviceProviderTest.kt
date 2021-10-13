package com.malinskiy.marathon.android

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.ddmlib.DdmlibDeviceProvider
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Clock

class AndroidDeviceProviderTest {
    @Test
    fun `terminate should close the channel`() {
        val vendorConfiguration = VendorConfiguration.AndroidConfiguration(
            File(""),
            applicationOutput = File(""),
            testApplicationOutput = File(""),
        )
        val configuration = Configuration(
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
        val provider = DdmlibDeviceProvider(
            configuration,
            AndroidTestBundleIdentifier(),
            vendorConfiguration,
            Track(),
            SystemTimer(Clock.systemDefaultZone())
        )

        runBlocking {
            provider.terminate()
        }

        provider.subscribe().isClosedForReceive shouldEqual true
        provider.subscribe().isClosedForSend shouldEqual true
    }
}
