package com.malinskiy.marathon.ios.cmd.local

import com.malinskiy.marathon.extension.withTimeoutOrNull
import com.malinskiy.marathon.ios.cmd.BaseCommand
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandHost
import com.malinskiy.marathon.ios.extensions.produceLinesManually
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

        val process = ProcessBuilder(command).apply {
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
        
        val stdout = produceLinesManually(job, process.inputStream, idleTimeout, charset, channelCapacity) { process.isAlive && !exitCode.isCompleted }
        val stderr = produceLinesManually(job, process.errorStream, idleTimeout, charset, channelCapacity) { process.isAlive && !exitCode.isCompleted }

       
        return KotlinProcessCommand(
            process, job, stdout, stderr, exitCode, destroyForcibly
        )
    }

    override fun close() {
    }
}

private const val POLL_DURATION_MILLIS = 1L
private suspend fun Process.suspendFor(): Int {
    while (isAlive) {
        delay(POLL_DURATION_MILLIS)
    }
    return exitValue()
}
