package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.log.MarathonLogging

class DebugLogPrinter (
    private val prefix: String = DebugLogPrinter::class.java.simpleName,
    private val hideRunnerOutput: Boolean
) : LineListener {
    private val logger by lazy {
        MarathonLogging.logger(prefix).also { it.trace("Mirroring remote logs with name '$prefix'") }
    }

    override suspend fun onLine(line: String) {
        if (!hideRunnerOutput)
            logger.debug(line)
    }
}
