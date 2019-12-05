package com.malinskiy.marathon.android.executor.listeners.line

class OutputLineListener : LineListener {
    private val builder = StringBuilder()
    val output: String
        get() = builder.toString()

    override fun onLine(line: String) {
        builder.appendln(line)
    }
}