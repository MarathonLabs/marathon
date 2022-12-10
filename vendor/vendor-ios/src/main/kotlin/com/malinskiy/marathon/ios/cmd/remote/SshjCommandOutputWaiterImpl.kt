package com.malinskiy.marathon.ios.cmd.remote

import kotlinx.coroutines.delay
import java.time.Duration

class SshjCommandOutputWaiterImpl(
    private val testOutputTimeoutMillis: Duration,
    private val sleepDurationMillis: Long
) : SshjCommandOutputWaiter {

    private var lastOutputTimeMillis = System.currentTimeMillis()

    override fun update() {
        lastOutputTimeMillis = System.currentTimeMillis()
    }

    override val isExpired: Boolean
        get() {
            if (testOutputTimeoutMillis > 0) {
                val timeSinceLastOutputMillis = System.currentTimeMillis() - lastOutputTimeMillis
                if (timeSinceLastOutputMillis > testOutputTimeoutMillis) {
                    return true
                }
            }
            return false
        }

    override suspend fun wait() {
        delay(sleepDurationMillis)
    }
}
