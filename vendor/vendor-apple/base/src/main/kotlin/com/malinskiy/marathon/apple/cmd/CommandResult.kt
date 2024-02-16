package com.malinskiy.marathon.apple.cmd

data class CommandResult(val stdout: List<String>, val stderr: List<String>, val exitCode: Int?) {
    val successful = exitCode == 0
    val combinedStdout: String by lazy {
        stdout.joinToString(separator = System.lineSeparator())
    }
    val combinedStderr: String by lazy {
        stderr.joinToString(separator = System.lineSeparator())
    }

    fun successfulOrNull(): CommandResult? {
        return if (!successful) {
            null
        } else {
            this
        }
    }
}
