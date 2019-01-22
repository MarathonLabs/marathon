package com.malinskiy.marathon.ios.cmd.remote

interface CommandExecutor {
    companion object {
        val DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS: Long
            get() = 900000L
        val DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS: Long
            get() = 45000L
    }

    fun startSession(command: String): CommandSession

    fun exec(command: String,
             timeoutMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
             testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS): CommandResult
    suspend fun exec(command: String,
                     timeoutMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                     testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS,
                     onLine: (String) -> Unit): Int?

    fun disconnect()
}

fun CommandExecutor.execOrNull(command: String,
                               timeoutMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                               testOutputTimeoutMillis: Long = CommandExecutor.DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS): CommandResult? =
    try {
        this.exec(command, timeoutMillis, testOutputTimeoutMillis)
    } catch (e: Exception) { null }
