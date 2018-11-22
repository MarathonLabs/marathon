package com.malinskiy.marathon.ios.cmd.remote

interface CommandExecutor {
    companion object {
        val DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS: Long
            get() = 30000L
    }

    fun startSession(command: String, timeoutMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS): CommandSession
    fun disconnect()

    fun exec(command: String, timeoutMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS): CommandResult
}
