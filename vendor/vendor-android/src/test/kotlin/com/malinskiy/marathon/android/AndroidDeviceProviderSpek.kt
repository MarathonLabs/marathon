package com.malinskiy.marathon.android

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class AndroidDeviceProviderSpek: Spek({
    given("A provider") {
        val provider = AndroidDeviceProvider()

        on("terminate") {
            it("should close the channel") {
                runBlocking {
                    provider.terminate()
                }

                provider.subscribe().isClosedForReceive shouldEqual true
                provider.subscribe().isClosedForSend shouldEqual true
            }
        }
    }
})
