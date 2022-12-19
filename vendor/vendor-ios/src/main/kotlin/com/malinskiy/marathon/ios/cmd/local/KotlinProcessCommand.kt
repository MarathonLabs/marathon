package com.malinskiy.marathon.ios.cmd.local

import com.malinskiy.marathon.ios.cmd.BaseCommand
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel

class KotlinProcessCommand(
    private val process: Process,
    job: CompletableJob,
    stdout: ReceiveChannel<String>,
    stderr: ReceiveChannel<String>,
    exitCode: Deferred<Int?>,
    private val destroyForcibly: Boolean,
) : BaseCommand(stdout, stderr, exitCode, job) {
    override val isAlive: Boolean
        get() = process.isAlive
    
    override fun terminate() {
        process.destroy()
    }

    override fun kill() {
        process.destroyForcibly()
    }

    override fun close() {
        when (destroyForcibly) {
            true -> process.destroyForcibly()
            false -> process.destroy()
        }
        super.close()
    }
}
