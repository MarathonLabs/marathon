package com.malinskiy.marathon.ios.xcrun

interface StreamingLogParser {
    fun onLine(line: String)

    fun close()
}
