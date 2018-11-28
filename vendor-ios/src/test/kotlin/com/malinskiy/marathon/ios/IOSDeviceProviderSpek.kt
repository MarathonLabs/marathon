package com.malinskiy.marathon.ios

import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class IOSDeviceProviderSpek: Spek({
    given("A provider") {
        val provider = IOSDeviceProvider()

        on("terminate") {
            it("should close the channel") {
                provider.terminate()

                provider.subscribe().isClosedForReceive shouldEqual true
                provider.subscribe().isClosedForSend shouldEqual true
            }
        }
    }
})