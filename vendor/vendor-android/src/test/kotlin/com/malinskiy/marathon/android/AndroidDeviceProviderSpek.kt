package com.malinskiy.marathon.android

import com.malinskiy.marathon.spek.initKoin
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class AndroidDeviceProviderSpek: Spek({
    initKoin()

    given("A provider") {
        on("terminate") {
            it("should close the channel") {
                val provider = AndroidDeviceProvider()

                runBlocking {
                    provider.terminate()
                }

                provider.subscribe().isClosedForReceive shouldEqual true
                provider.subscribe().isClosedForSend shouldEqual true
            }
        }
    }
})
