package com.malinskiy.marathon.ios.xcrun

class CompositeLogParser(val parsers: Collection<StreamingLogParser>) : StreamingLogParser {
    override fun close() {
        parsers.forEach { it.close() }
    }

    override fun onLine(line: String) {
        parsers.forEach { it.onLine(line) }
    }
}
