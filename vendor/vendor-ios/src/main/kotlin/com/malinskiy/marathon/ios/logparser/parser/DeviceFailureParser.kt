package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.ios.logparser.StreamingLogParser

class DeviceFailureParser: StreamingLogParser {
    private val patterns = listOf(
        "Failed to install or launch the test runner",
        "Software caused connection abort",
        "Unable to find a destination matching the provided destination specifier",
        "Terminating since there is no system app",
        "Exiting because the workspace server has disconnected"
    )
    private var count = 0
    override fun onLine(line: String) {
        patterns.firstOrNull { line.contains(it) }
            ?.let {
                throw DeviceFailureException(
                    when (patterns.indexOf(it)) {
                        0 -> DeviceFailureReason.FailedRunner
                        1 -> DeviceFailureReason.ConnectionAbort
                        2 -> DeviceFailureReason.MissingDestination
                        else -> DeviceFailureReason.Unknown
                    },
                    it
                )
            }
    }

    override fun close() = Unit
}
