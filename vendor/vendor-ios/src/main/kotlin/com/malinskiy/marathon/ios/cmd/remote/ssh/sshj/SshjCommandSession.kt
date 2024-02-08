package com.malinskiy.marathon.ios.cmd.remote.ssh.sshj

import com.malinskiy.marathon.ios.cmd.BaseCommand
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.connection.channel.direct.Signal
import java.util.concurrent.atomic.AtomicBoolean

class SshjCommandSession(
    private val command: Session.Command,
    job: CompletableJob,
    stdout: ReceiveChannel<String>,
    stderr: ReceiveChannel<String>,
    exitCode: Deferred<Int?>,
    private val terminateSignal: Signal = Signal.TERM,
    private val killSignal: Signal = Signal.KILL,
) : BaseCommand(stdout, stderr, exitCode, job) {
    private var closed = AtomicBoolean(false)
    override val isAlive = command.isOpen

    override fun terminate() {
        command.signal(terminateSignal)
    }

    override fun kill() {
        command.signal(killSignal)
    }

    override fun close() {
        if (!closed.getAndSet(true)) {
            if (command.isOpen) {
                terminate()
            }
            command.close()
            command.join()
            super.close()
        }
    }
}
