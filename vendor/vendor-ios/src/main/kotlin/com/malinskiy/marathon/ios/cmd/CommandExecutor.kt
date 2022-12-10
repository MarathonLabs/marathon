package com.malinskiy.marathon.ios.cmd

import com.malinskiy.marathon.ios.cmd.remote.CommandSession
import com.malinskiy.marathon.log.MarathonLogging
import java.io.Closeable
import java.time.Duration

interface CommandExecutor : Closeable {
    companion object {
        val DEFAULT_SSH_CONNECTION_TIMEOUT: Duration = Duration.ofSeconds(900)
        val DEFAULT_SSH_NO_OUTPUT_TIMEOUT: Duration = Duration.ofSeconds(45)
    }
    
    val workerId: String

    fun startSession(command: String): CommandSession

    fun execBlocking(
        command: String,
        timeout: Duration = DEFAULT_SSH_CONNECTION_TIMEOUT,
        idleTimeout: Duration = DEFAULT_SSH_NO_OUTPUT_TIMEOUT,
    ): CommandResult

    suspend fun execInto(
        command: String,
        timeout: Duration = DEFAULT_SSH_CONNECTION_TIMEOUT,
        idleTimeout: Duration = DEFAULT_SSH_NO_OUTPUT_TIMEOUT,
        onLine: (String) -> Unit,
    ): Int?

    suspend fun exec(
        command: String,
        timeout: Duration = DEFAULT_SSH_CONNECTION_TIMEOUT,
        idleTimeout: Duration = DEFAULT_SSH_NO_OUTPUT_TIMEOUT,
    ) {
        execInto(command, timeout, idleTimeout) { }
    }

    fun execOrNull(
        command: String,
        timeout: Duration = DEFAULT_SSH_CONNECTION_TIMEOUT,
        idleTimeout: Duration = DEFAULT_SSH_NO_OUTPUT_TIMEOUT,
    ): CommandResult? =
        try {
            execBlocking(command, timeout, idleTimeout)
        } catch (e: Exception) {
            MarathonLogging.logger(this::class.java.simpleName).warn("Exception caught executing $command: $e");
            null
        }
}

