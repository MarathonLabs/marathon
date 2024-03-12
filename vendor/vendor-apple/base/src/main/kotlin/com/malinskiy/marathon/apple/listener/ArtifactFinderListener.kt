package com.malinskiy.marathon.apple.listener

import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.log.MarathonLogging

open class ArtifactFinderListener(
    private val pattern: Regex,
    private val message: String,
    private val unique: Boolean = true
) : AppleTestRunListener, LineListener {
    private val logger = MarathonLogging.logger {}
    private var matches = mutableSetOf<String>()
    override suspend fun onLine(line: String) {
        pattern.find(line)?.groupValues?.firstOrNull()
            ?.let { matches.add(it.trim()) }
    }

    override suspend fun afterTestRun() {
        if (matches.isNotEmpty()) {
            val result = if (unique) {
                hashSetOf(*matches.toTypedArray())
            } else {
                matches
            }
            logger.info { "$message ${result.joinToString(", ")}" }
        }
    }
}
