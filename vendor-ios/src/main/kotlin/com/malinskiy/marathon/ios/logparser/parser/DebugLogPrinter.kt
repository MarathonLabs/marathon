package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.ios.logparser.StreamingLogParser
import com.malinskiy.marathon.log.MarathonLogging

class DebugLogPrinter(private val prefix: String = DebugLogPrinter::class.java.simpleName) : StreamingLogParser {
    private val logger by lazy {
        MarathonLogging.logger(prefix).also { it.trace("Mirroring remote logs with name '$prefix'") }
    }

    override fun onLine(line: String) {
        logger.debug(line)
    }
    override fun close() = Unit
}
