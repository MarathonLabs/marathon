package com.malinskiy.marathon.execution.listener

interface LineListener : AutoCloseable {
    fun onLine(line: String)

    override fun close() {}
}
