package com.malinskiy.marathon.ios

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import java.time.Clock

class IOSDeviceProviderTest {
    private val provider = IOSDeviceProvider(Track(), SystemTimer(Clock.systemDefaultZone()))

    @Test
    fun `should close the channel`() {
        runBlocking {
            provider.terminate()
        }

        provider.subscribe().isClosedForReceive shouldEqual true
        provider.subscribe().isClosedForSend shouldEqual true
    }
}
