package com.malinskiy.marathon.android

import com.malinskiy.marathon.analytics.internal.pub.Track
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class AndroidDeviceProviderSpek : Spek(
    {
        given("A provider") {
            on("terminate") {
                it("should close the channel") {
                    val provider = AndroidDeviceProvider(Track())

                    runBlocking {
                        provider.terminate()
                    }

                    provider.subscribe().isClosedForReceive shouldEqual true
                    provider.subscribe().isClosedForSend shouldEqual true
                }
            }
        }
    })
