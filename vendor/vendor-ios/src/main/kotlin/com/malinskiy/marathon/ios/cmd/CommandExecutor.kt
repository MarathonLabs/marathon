package com.malinskiy.marathon.ios.cmd

import com.malinskiy.marathon.ios.cmd.remote.CommandSession
import com.malinskiy.marathon.log.MarathonLogging
import java.io.Closeable

interface CommandExecutor : Closeable {
    companion object {
        val DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS: Long
            get() = 900000L
        val DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS: Long
            get() = 45000L
    }
    
    val workerId: String

    fun startSession(command: String): CommandSession

    fun execBlocking(
        command: String,
        maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
        testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS
    ): CommandResult

    suspend fun execInto(
        command: String,
        maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
        testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS,
        onLine: (String) -> Unit
    ): Int?

    suspend fun exec(
        command: String,
        maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
        testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS
    ) {
        execInto(command, maxExecutionDurationMillis, testOutputTimeoutMillis) { }
    }

    fun execOrNull(
        command: String,
        maxExecutionDurationMillis: Long = DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
        testOutputTimeoutMillis: Long = DEFAULT_SSH_NO_OUTPUT_TIMEOUT_MILLIS
    ): CommandResult? =
        try {
            execBlocking(command, maxExecutionDurationMillis, testOutputTimeoutMillis)
        } catch (e: Exception) {
            MarathonLogging.logger(this::class.java.simpleName).warn("Exception caught executing $command: $e");
            null
        }
}

