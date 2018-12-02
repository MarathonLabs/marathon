package com.malinskiy.marathon.ios.cmd.remote

interface CommandExecutor {
    companion object {
        val DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS: Long
            get() = 900000L
    }

    fun startSession(command: String, timeoutMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS): CommandSession

    fun exec(command: String, testOutputTimeoutMillis: Long = 0): CommandResult
    suspend fun exec(command: String, testOutputTimeoutMillis: Long = 0, onLine: (String) -> Unit): Int?

    fun disconnect()
}
