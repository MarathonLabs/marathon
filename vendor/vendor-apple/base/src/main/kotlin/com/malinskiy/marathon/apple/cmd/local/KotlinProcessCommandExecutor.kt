package com.malinskiy.marathon.apple.cmd.local

import com.malinskiy.marathon.apple.cmd.BaseCommand
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandHost
import com.malinskiy.marathon.apple.extensions.produceLinesManually
import com.malinskiy.marathon.extension.withTimeoutOrNull
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import java.io.File
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

/**
 * Note: doesn't support idle timeout currently
 */
class KotlinProcessCommandExecutor(
    private val destroyForcibly: Boolean = false,
    private val channelCapacity: Int = Channel.BUFFERED
) : CommandExecutor, CoroutineScope {
    override val logger = MarathonLogging.logger {}
    override val host: CommandHost = KotlinProcessHost()
    override val coroutineContext = Dispatchers.IO
    override val connected = true

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun execute(
        command: List<String>,
        timeout: Duration,
        idleTimeout: Duration,
        env: Map<String, String>,
        workdir: String?,
        charset: Charset,
    ): BaseCommand {
        val job = SupervisorJob()

        /**
         * For some reason local shell execution doesn't like leading " or '
         */
        val unescapedCommand = command.map {
            if(it.isShellscaped()) {
                it.removeSurrounding("\"").removeSurrounding("\'")
            } else {
                it
            }
        }

        val process = ProcessBuilder(unescapedCommand).apply {
            redirectOutput(ProcessBuilder.Redirect.PIPE)
            redirectError(ProcessBuilder.Redirect.PIPE)

            workdir?.let { directory(File(it)) }
            environment().putAll(env)
        }.start()
        
        val exitCode: Deferred<Int?> = async(job) {
            withTimeoutOrNull(timeout) {
                process.suspendFor()
            }
        }

        val lastOutputTimeMillis = AtomicLong(System.currentTimeMillis())
        val stdout = produceLinesManually(job, process.inputStream, lastOutputTimeMillis, idleTimeout, charset, channelCapacity) { process.isAlive && !exitCode.isCompleted }
        val stderr = produceLinesManually(job, process.errorStream, lastOutputTimeMillis, idleTimeout, charset, channelCapacity) { process.isAlive && !exitCode.isCompleted }

        return KotlinProcessCommand(
            process, job, stdout, stderr, exitCode, destroyForcibly
        )
    }

    override fun close() {
    }
}

/**
 * Verifies only fully escaped strings without verifying special characters
 */
private fun String.isShellscaped(): Boolean {
    return (startsWith('\'') && endsWith('\'')) ||
        (startsWith('"') && endsWith('"'))
}

private const val POLL_DURATION_MILLIS = 1L
private suspend fun Process.suspendFor(): Int {
    while (isAlive) {
        delay(POLL_DURATION_MILLIS)
    }
    return exitValue()
}
