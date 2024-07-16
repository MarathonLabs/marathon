package com.malinskiy.marathon.android.logcat

import com.malinskiy.marathon.android.adam.log.LogCatMessage

interface LogcatListener : AutoCloseable {
    suspend fun onMessage(message: LogCatMessage)

    override fun close() {}
}
