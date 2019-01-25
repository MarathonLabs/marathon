package com.malinskiy.marathon.ios.cmd.remote

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.OpenFailException
import net.schmizz.sshj.connection.channel.direct.Signal
import java.io.InputStream
import java.io.OutputStream

class SshjCommandSession(executableLine: String, ssh: SSHClient): CommandSession {
    private val session = ssh.startSession()
    private val command = session.exec(executableLine)

    override val inputStream: InputStream
        get() = command.inputStream
    override val errorStream: InputStream
        get() = command.errorStream
    override val outputStream: OutputStream
        get() = command.outputStream

    override val isEOF: Boolean
        get() = command.isEOF

    override val isOpen: Boolean
        get() = command.isOpen

    override fun kill() {
        command.signal(Signal.TERM)
    }

    override fun close() { command.close() }

    override val exitStatus: Int?
        get() = command.exitStatus
}
