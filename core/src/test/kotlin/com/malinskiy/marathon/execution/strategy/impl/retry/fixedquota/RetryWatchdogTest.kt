package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

class RetryWatchdogTest {

    @Test
    fun `total allowed quota tests, should return false if total allowed quota is 0`() {
        val watchdog = RetryWatchdog(0, 3)
        watchdog.requestRetry(1) shouldBe false
    }

    @Test
    fun `total allowed quota tests, should return true if total allowed quota is not 0`() {
        val watchdog = RetryWatchdog(100, 3)
        watchdog.requestRetry(1) shouldBe true
    }

    @Test
    fun `max retry per test quota, should return false if max retry per test quota is 0 and input is 1`() {
        val watchdog = RetryWatchdog(100, 0)
        watchdog.requestRetry(1) shouldBe false
    }

    @Test
    fun `max retry per test quota, should return false if max retry per test quota is 2 and input is 3`() {
        val watchdog = RetryWatchdog(100, 2)
        watchdog.requestRetry(3) shouldBe false
    }

    @Test
    fun `max retry per test quota, should return true if max retry per test quota is 3 and input is 1`() {
        val watchdog = RetryWatchdog(100, 3)
        watchdog.requestRetry(1) shouldBe true
    }

    @Test
    fun `max retry per test quota, should return true if max retry per test quota is 2 and input is 1`() {
        val watchdog = RetryWatchdog(100, 2)
        watchdog.requestRetry(1) shouldBe true
    }
}
