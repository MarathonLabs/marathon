package com.malinskiy.marathon.ios.cmd.remote

interface SshjCommandOutputWaiter {
    fun update()
    val isExpired: Boolean
    suspend fun wait()
}
