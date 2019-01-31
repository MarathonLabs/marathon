package com.malinskiy.marathon.ios.cmd.remote

import com.malinskiy.marathon.log.MarathonLogging

interface CommandExecutor {
    companion object {
        val DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS: Long
            get() = 900000L
        val DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS: Long
            get() = 45000L
    }

    fun startSession(command: String): CommandSession

    fun exec(command: String,
             maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
             testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS): CommandResult
    suspend fun exec(command: String,
                     maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                     testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS,
                     onLine: (String) -> Unit): Int?

    suspend fun execAsync(command: String,
                          maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                          testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS): CommandResult

    fun disconnect()
}

fun CommandExecutor.execOrNull(command: String,
                               maxExecutionDurationMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                               testOutputTimeoutMillis: Long = CommandExecutor.DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS): CommandResult? =
    try {
        exec(command, maxExecutionDurationMillis, testOutputTimeoutMillis)
    } catch (e: Exception) {
        MarathonLogging.logger(this::class.java.simpleName).warn("Exception caught executing $command: $e");
        null
    }

suspend fun CommandExecutor.execAsyncOrNull(command: String,
                                            maxExecutionDurationMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                                            testOutputTimeoutMillis: Long = CommandExecutor.DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS): CommandResult? =
        try {
            execAsync(command, maxExecutionDurationMillis, testOutputTimeoutMillis)
        } catch (e: Exception) {
            MarathonLogging.logger(this::class.java.simpleName).warn("Exception caught executing $command: $e");
            null
        }
