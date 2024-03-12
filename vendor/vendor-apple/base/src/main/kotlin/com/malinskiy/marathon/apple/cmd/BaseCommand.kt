package com.malinskiy.marathon.apple.cmd

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseCommand(
    override val stdout: ReceiveChannel<String>,
    override val stderr: ReceiveChannel<String>,
    override val exitCode: Deferred<Int?>,
    protected val job: CompletableJob,
) : CommandSession {
    override suspend fun await(): CommandResult = withContext(Dispatchers.IO) {
        val deferredStdout = supervisorScope {
            async(job) {
                val buffer = mutableListOf<String>()
                while (true) {
                    val channelResult = stdout.receiveCatching()
                    channelResult.onSuccess { buffer.add(it) }
                    channelResult.onClosed { if (it != null) cancel(CancellationException("Channel closed", it)) }
                    channelResult.onFailure { if (it != null) cancel(CancellationException("Channel failed", it)) }

                    if (!channelResult.isSuccess) break
                }
                buffer
            }
        }

        val deferredStderr = supervisorScope {
            async(job) {
                val buffer = mutableListOf<String>()
                while (true) {
                    val channelResult = stderr.receiveCatching()
                    channelResult.onSuccess { buffer.add(it) }
                    channelResult.onClosed { if (it != null) cancel(CancellationException("Channel closed", it)) }
                    channelResult.onFailure { if (it != null) cancel(CancellationException("Channel failed", it)) }

                    if (!channelResult.isSuccess) break
                }
                buffer
            }
        }



        val (out, err, exitCode) = awaitAll(deferredStdout, deferredStderr, exitCode)

        CommandResult(out as List<String>, err as List<String>, exitCode as Int?)
    }

    override suspend fun drain() {
        return supervisorScope {
            async(job) {
                for (line in stdout) {
                }
            }
            async(job) {
                for (line in stderr) {
                }
            }
        }
    }

    override fun close() {
        if (job.isActive) {
            runBlocking {
                job.cancelAndJoin()
            }
        }
    }
}
