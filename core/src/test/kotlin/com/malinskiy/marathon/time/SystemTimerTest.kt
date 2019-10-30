package com.malinskiy.marathon.time

import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Clock

class SystemTimerSpek : Spek(
    {

        val clock = mock<Clock>()
        val timer = SystemTimer(clock)
        beforeEachTest {
            reset(clock)
        }

        describe("SystemTimer") {
            it("should call passed clock to get currentTimeMillis") {
                whenever(clock.millis()).thenReturn(100)

                timer.currentTimeMillis()
                    .shouldEqualTo(100)
            }

            it("should call passed clock to measure") {
                var counter = 0L
                whenever(clock.millis()).thenAnswer {
                    counter++ * 1000L
                }

                timer.measure { }.shouldEqualTo(1000L)
            }
        }
    })