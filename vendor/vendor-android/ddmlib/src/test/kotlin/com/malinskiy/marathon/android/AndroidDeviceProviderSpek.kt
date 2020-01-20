package com.malinskiy.marathon.android

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.ddmlib.DdmlibDeviceProvider
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.time.Clock

class AndroidDeviceProviderSpek : Spek(
    {
        given("A provider") {
            on("terminate") {
                it("should close the channel") {
                    val provider = DdmlibDeviceProvider(Track(), SystemTimer(Clock.systemDefaultZone()), mock(), mock())

                    runBlocking {
                        provider.terminate()
                    }

                    provider.subscribe().isClosedForReceive shouldEqual true
                    provider.subscribe().isClosedForSend shouldEqual true
                }
            }
        }
    })
