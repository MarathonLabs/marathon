package com.malinskiy.marathon.execution.listener

interface LineListener : AutoCloseable {
    suspend fun onLine(line: String)

    override fun close() {}
}
