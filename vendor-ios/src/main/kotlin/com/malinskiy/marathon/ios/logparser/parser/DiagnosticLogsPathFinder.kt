package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.ios.logparser.StreamingLogParser

class DiagnosticLogsPathFinder: StreamingLogParser {

    private val logPathPattern = """(^\s*|\s+)/[^\s]+\.log\s*$""".toRegex()
    private var paths = arrayListOf<String>()

    val diagnosticLogPaths: Collection<String>
        get() = paths

    override fun onLine(line: String) {
        logPathPattern.find(line)?.groupValues?.firstOrNull()
                ?.let { paths.add(it) }
    }

    override fun close() = Unit
}
