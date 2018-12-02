package com.malinskiy.marathon.ios.cmd.remote

import kotlinx.coroutines.experimental.delay

class SshjCommandOutputTimeoutHandlerImpl(
        private val testOutputTimeoutMillis: Long,
        private val sleepDurationMillis: Long): SshjCommandOutputTimeoutHandler {

    private var lastOutputTimeMillis = System.currentTimeMillis()

    override fun update() {
        lastOutputTimeMillis = System.currentTimeMillis()
    }

    override suspend fun getIsUnresponsiveAndWait(): Boolean {
        if (testOutputTimeoutMillis > 0) {
            val timeSinceLastOutputMillis = System.currentTimeMillis() - lastOutputTimeMillis
            if (timeSinceLastOutputMillis > testOutputTimeoutMillis) {
                return true
            }
            delay(sleepDurationMillis)
        }
        return false
    }
}
