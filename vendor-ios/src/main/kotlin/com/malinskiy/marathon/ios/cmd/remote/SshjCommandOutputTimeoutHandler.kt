package com.malinskiy.marathon.ios.cmd.remote

interface SshjCommandOutputTimeoutHandler {
    fun update()
    suspend fun getIsUnresponsiveAndWait(): Boolean
}
