package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.execution.listener.LineListener

class DiagnosticLogsPathFinder : LineListener {

    private val logPathPattern = """(^\s*|\s+)/.+\.log\s*$""".toRegex()
    private var paths = mutableSetOf<String>()

    val diagnosticLogPaths: Collection<String>
        get() = paths

    override fun onLine(line: String) {
        logPathPattern.find(line)?.groupValues?.firstOrNull()
            ?.let { paths.add(it) }
    }
}
