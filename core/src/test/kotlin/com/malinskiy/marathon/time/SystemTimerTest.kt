package com.malinskiy.marathon.time

import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock

class SystemTimerTest {
    private val clock = mock<Clock>()
    private val timer = SystemTimer(clock)

    @BeforeEach
    fun `reset mocks`() {
        reset(clock)
    }

    @Test
    fun `should call passed clock to get currentTimeMillis`() {
        whenever(clock.millis()).thenReturn(100)

        timer.currentTimeMillis()
            .shouldEqualTo(100)
    }

    @Test
    fun `should call passed clock to measure`() {
        var counter = 0L
        whenever(clock.millis()).thenAnswer {
            counter++ * 1000L
        }

        timer.measure { }.shouldEqualTo(1000L)
    }
}
