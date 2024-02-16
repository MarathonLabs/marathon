package com.malinskiy.marathon.apple.cmd

import com.malinskiy.marathon.apple.extensions.Durations
import mu.KLogger
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.transport.TransportException
import java.nio.charset.Charset
import java.time.Duration

interface CommandExecutor : AutoCloseable {
    val host: CommandHost
    val logger: KLogger
    val connected: Boolean

    /**
     * Executes [command]
     *
     * It will never throw timeout exceptions. If the command is interrupted in parallel the channels should be drained and a null exit
     * code returned
     *
     * The call might block for some portion of execution time since the APIs might not support full asynchronous execution
     *
     * WARNING: it is the responsibility of the caller to fully drain the resulting channels. Not doing so can lead to locks
     *
     * @param [command] Runs this command on some system. For example: `pwd`
     * @param [timeout] Limits the amount of time spent on waiting for [command] to finish
     * @param [idleTimeout] Limits the amount of time spent on waiting for [command] to provide more output
     * @param [env] Additional environment variables for [command].
     * @param [workdir] Working directory for [command]
     * @param [charset] Command output is interpreted line by line with this encoding
     */
    suspend fun execute(
        command: List<String>,
        timeout: Duration,
        idleTimeout: Duration,
        env: Map<String, String>,
        workdir: String?,
        charset: Charset = host.charset,
    ): CommandSession

    suspend fun criticalExecute(timeout: Duration, vararg arg: String) =
        criticalExecute(listOf(*arg), timeout, Durations.INFINITE, emptyMap(), null)

    suspend fun criticalExecute(
        command: List<String>,
        timeout: Duration,
        idleTimeout: Duration,
        env: Map<String, String>,
        workdir: String?,
        charset: Charset = host.charset,
    ): CommandResult {
        val cmd = execute(command, timeout, idleTimeout, env, workdir, charset)
        val result = cmd.await()
        if (!result.successful) {
            throw CommandException(
                """command ${command.joinToString(" ")} failed with exit code ${result.exitCode}:
                   stdout: ${result.stdout.joinToString(System.lineSeparator())}
                   stderr: ${result.stderr.joinToString(System.lineSeparator())}
                """.trimMargin()
            )
        }
        return result
    }

    suspend fun safeExecute(timeout: Duration, vararg args: String) =
        safeExecute(listOf(*args), timeout, Durations.INFINITE, emptyMap(), null)

    suspend fun safeExecute(
        command: List<String>,
        timeout: Duration,
        idleTimeout: Duration,
        env: Map<String, String>,
        workdir: String?,
        charset: Charset = host.charset,
    ): CommandResult? {
        return try {
            val cmd = execute(command, timeout, idleTimeout, env, workdir, charset)
            cmd.await()
        } catch (e: ConnectionException) {
            logger.error("Unable to start a remote shell session $command")
            null
        } catch (e: TransportException) {
            logger.error("Error starting a remote shell session $command")
            null
        }
    }

    suspend fun safeExecuteNohup(pidfile: String, timeout: Duration, vararg args: String) =
        safeExecuteNohup(pidfile, timeout, Durations.INFINITE, emptyMap(), null, *args)


    /**
     * nohup requires us to redirect all streams, otherwise ssh transport will just hang
     */
    suspend fun safeExecuteNohup(
        pidfile: String,
        timeout: Duration,
        idleTimeout: Duration,
        env: Map<String, String> = emptyMap(),
        workdir: String? = null,
        vararg cmd: String
    ): CommandResult? {
        val command = listOf("sh", "-c", "nohup ${cmd.joinToString(" ")} > /dev/null 2>&1 & echo $! > $pidfile")
        return safeExecute(command, timeout, idleTimeout, env, workdir)
    }
}
