package com.malinskiy.marathon.android.executor.listeners.line

/**
 * Ignores the output
 */
class NullOutputListener : LineListener {
    override fun onLine(line: String) {}
}
