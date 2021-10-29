package com.malinskiy.marathon.android

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.ddmlib.DdmlibDeviceProvider
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Clock

class AndroidDeviceProviderTest {
    @Test
    fun `terminate should close the channel`() {
        val vendorConfiguration = VendorConfiguration.AndroidConfiguration(
            androidSdk = File(""),
            applicationOutput = File(""),
            testApplicationOutput = File(""),
        )
        val configuration = Configuration.Builder(
            name = "",
            outputDir = File(""),
            vendorConfiguration = vendorConfiguration,
        ).apply { analyticsTracking = false }.build()
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

        provider.subscribe().isClosedForReceive shouldBeEqualTo true
        provider.subscribe().isClosedForSend shouldBeEqualTo true
    }
}
