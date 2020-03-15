package com.malinskiy.marathon.android

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.ddmlib.DdmlibDeviceProvider
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import java.time.Clock

class AndroidDeviceProviderTest {
    @Test
    fun `terminate should close the channel`() {
        val provider = DdmlibDeviceProvider(Track(), SystemTimer(Clock.systemDefaultZone()))

        runBlocking {
            provider.terminate()
        }

        provider.subscribe().isClosedForReceive shouldEqual true
        provider.subscribe().isClosedForSend shouldEqual true
    }
}
