package com.malinskiy.marathon.apple.ios.cmd

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

abstract class BaseCommand(
    override val stdout: ReceiveChannel<String>,
    override val stderr: ReceiveChannel<String>,
    override val exitCode: Deferred<Int?>,
    protected val job: CompletableJob,
) : CommandSession {
    override suspend fun await(): CommandResult = withContext(Dispatchers.IO) {
        val deferredStdout = supervisorScope {
            async(job) {
                val stdoutBuffer = mutableListOf<String>()
                for (line in stdout) {
                    stdoutBuffer.add(line)
                }
                stdoutBuffer
            }
        }

        val deferredStderr = supervisorScope {
            async(job) {
                val stderrBuffer = mutableListOf<String>()
                for (line in stderr) {
                    stderrBuffer.add(line)
                }
                stderrBuffer
            }
        }

        val out = deferredStdout.await()
        val err = deferredStderr.await()
        val exitCode = exitCode.await()

        CommandResult(out, err, exitCode)
    }

    override suspend fun drain() {
        return supervisorScope {
            async(job) {
                for (line in stdout) {}
            }
            async(job) {
                for (line in stderr) {}
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
