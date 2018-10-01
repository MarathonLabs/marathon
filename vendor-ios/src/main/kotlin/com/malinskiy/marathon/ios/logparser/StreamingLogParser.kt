package com.malinskiy.marathon.ios.logparser

interface StreamingLogParser {
    fun onLine(line: String)

    fun close()
}
