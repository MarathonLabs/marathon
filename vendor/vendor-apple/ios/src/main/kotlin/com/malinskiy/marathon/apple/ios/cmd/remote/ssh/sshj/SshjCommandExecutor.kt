package com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj

import com.malinskiy.marathon.apple.ios.cmd.CommandExecutor
import com.malinskiy.marathon.apple.ios.cmd.CommandSession
import com.malinskiy.marathon.apple.ios.extensions.produceLinesManually
import com.malinskiy.marathon.extension.withTimeoutOrNull
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.channel.direct.Session.Command
import net.schmizz.sshj.connection.channel.direct.Signal
import java.io.IOException
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

class SshjCommandExecutor(
    override val host: SshjCommandHost,
    private val client: SSHClient,
    private val channelCapacity: Int,
) : CommandExecutor, CoroutineScope {
    override val logger = MarathonLogging.logger {}
    override val coroutineContext = Dispatchers.IO
    override val connected: Boolean
        get() = client.isConnected

    override suspend fun execute(
        command: List<String>,
        timeout: Duration,
        idleTimeout: Duration,
        env: Map<String, String>,
        workdir: String?,
        charset: Charset
    ): CommandSession {
        val job = SupervisorJob()

        val session = client.startSession()
        val escapedCmd = command.joinToString(" ") {
            if (it.isShellscaped()) {
                it
            } else if (it.containsShellSpecialChars()) {
                "\'" + it.replace("\'", "\\'") + "\'"
            } else {
                it
            }
        }
        val cmd = session.exec(escapedCmd)
        
        val stdout = produceLinesManually(job, cmd.inputStream, idleTimeout, charset, channelCapacity) { cmd.isOpen && !cmd.isEOF }
        val stderr = produceLinesManually(job, cmd.errorStream, idleTimeout, charset, channelCapacity) { cmd.isOpen && !cmd.isEOF }
        val exitCode: Deferred<Int?> = async(job) {
            val result = withTimeoutOrNull(timeout) {
                cmd.suspendFor()
            }
            if (result == null) {
                cmd.signal(Signal.TERM)
                cmd.close()
            }
            result
        }

        return SshjCommandSession(cmd, job, stdout, stderr, exitCode)
    }

    override fun close() {
        try {
            client.close()
        } catch (e: IOException) {
            logger.error(e) { "Error disconnecting ${host.addr}${host.port}" }
        }
    }
}

/**
 * Verifies only fully escaped strings without verifying special characters
 */
private fun String.isShellscaped(): Boolean {
    return (startsWith('\'') && endsWith('\'')) ||
        (startsWith('"') && endsWith('"'))
}

private val SHELL_SPECIAL_CHARS = setOf('"',' ','$','\'','\\','#','=','[',']','!','>','<','|',';','{','}','(',')','*','?','~','&')
private fun String.containsShellSpecialChars(): Boolean {
    return any { 
        SHELL_SPECIAL_CHARS.contains(it)
    }
}

private val pollDurationMillis = 1L
private suspend fun Command.suspendFor(): Int? {
    do {
        try {
            join(1, TimeUnit.NANOSECONDS)
            break
        } catch (ex: ConnectionException) {
            delay(pollDurationMillis)
        }
    } while (coroutineContext.isActive)
    close()
    return exitStatus ?: autodetectExitCode(exitSignal)
}

fun autodetectExitCode(exitSignal: Signal?): Int? {
    return exitSignal?.let {
        when (it) {
            Signal.TERM -> 143
            else -> {
                null
            }
        }
    }
}

