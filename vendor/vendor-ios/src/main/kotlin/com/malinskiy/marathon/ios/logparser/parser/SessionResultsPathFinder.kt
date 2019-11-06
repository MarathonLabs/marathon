package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.ios.logparser.StreamingLogParser

class SessionResultsPathFinder : StreamingLogParser {
    private val resultPathPattern = """(^\s*|\s+)/.+\.xcresult\s*$""".toRegex()
    private var paths = arrayListOf<String>()

    val resultPaths: Collection<String>
        get() = paths

    override fun onLine(line: String) {
        resultPathPattern.find(line)?.groupValues?.firstOrNull()
            ?.let { paths.add(it) }
    }

    override fun close() = Unit
}
