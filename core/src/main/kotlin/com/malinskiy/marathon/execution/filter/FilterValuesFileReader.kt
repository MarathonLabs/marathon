package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

class FilterValuesFileReader {
    private val log = MarathonLogging.logger("FilterValuesFileReader")

    fun readValues(file: File?) = file?.let { valuesFile ->
        if (valuesFile.exists()) {
            valuesFile.readLines().filter { it.isNotBlank() && !it.isCommentLine() }.map { it.trimCommentFromLine() }
        } else {
            log.error { "Filtering configuration file ${valuesFile.absoluteFile} does not exist. Applying empty list." }
            emptyList()
        }
    }

    private fun String.trimCommentFromLine() = this.replace("\\s+#.*$".toRegex(), "").trim()

    private fun String.isCommentLine(): Boolean {
        return this.trim().startsWith("#")
    }
}
