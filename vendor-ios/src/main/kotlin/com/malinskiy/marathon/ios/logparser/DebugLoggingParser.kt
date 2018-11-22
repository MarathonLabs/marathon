package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.log.MarathonLogging

class DebugLoggingParser() : StreamingLogParser {
    override fun close() {}

    val logger = MarathonLogging.logger(DebugLoggingParser::class.java.simpleName)

    override fun onLine(line: String) {
        logger.debug { line }
    }
}
