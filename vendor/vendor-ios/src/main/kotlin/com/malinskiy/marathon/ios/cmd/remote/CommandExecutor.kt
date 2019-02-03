package com.malinskiy.marathon.ios.cmd.remote

import com.malinskiy.marathon.log.MarathonLogging
import java.io.Closeable

interface CommandExecutor: Closeable {
    companion object {
        val DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS: Long
            get() = 900000L
        val DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS: Long
            get() = 45000L
    }

    fun startSession(command: String): CommandSession

    fun execBlocking(command: String,
                     maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                     testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS): CommandResult
    suspend fun execInto(command: String,
                         maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                         testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS,
                         onLine: (String) -> Unit): Int?
}

suspend fun CommandExecutor.exec(
        command: String,
        maxExecutionDurationMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
        testOutputTimeoutMillis: Long = CommandExecutor.DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS) {
    execInto(command, maxExecutionDurationMillis, testOutputTimeoutMillis) { }
}

fun CommandExecutor.execOrNull(command: String,
                               maxExecutionDurationMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                               testOutputTimeoutMillis: Long = CommandExecutor.DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS): CommandResult? =
    try {
        execBlocking(command, maxExecutionDurationMillis, testOutputTimeoutMillis)
    } catch (e: Exception) {
        MarathonLogging.logger(this::class.java.simpleName).warn("Exception caught executing $command: $e");
        null
    }

