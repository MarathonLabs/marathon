package com.malinskiy.marathon.execution.listener

class CompositeLineListener(private val delegates: Collection<LineListener>) : LineListener {
    override suspend fun onLine(line: String) {
        delegates.forEach { it.onLine(line) }
    }

    override fun close() {
        delegates.forEach { it.close() }
    }
}
