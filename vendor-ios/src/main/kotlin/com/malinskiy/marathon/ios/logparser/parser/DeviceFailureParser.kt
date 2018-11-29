package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.ios.logparser.StreamingLogParser

class DeviceFailureParser: StreamingLogParser {
    private val patterns = listOf(
        "Failed to install or launch the test runner",
        "Software caused connection abort",
        "Unable to find a destination matching the provided destination specifier"
    )
    override fun onLine(line: String) {
        patterns.firstOrNull { line.contains(it) }
            ?.let {
                throw DeviceFailureException(it)
            }
    }

    override fun close() = Unit
}
