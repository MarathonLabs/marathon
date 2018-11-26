package com.malinskiy.marathon.ios.cmd.remote

import net.schmizz.sshj.SSHClient
import java.io.InputStream
import java.io.OutputStream
import java.time.Duration
import java.util.concurrent.TimeUnit

class SshjCommandSession(executableLine: String, ssh: SSHClient, private val timeoutMillis: Long): CommandSession {

    private val session = ssh.startSession()
    private val command = session.exec(executableLine)

    override val inputStream: InputStream
        get() = command.inputStream
    override val errorStream: InputStream
        get() = command.errorStream
    override val outputStream: OutputStream
        get() = command.outputStream

    override fun close() { command.close() }

    override val exitStatus: Int?
        get() = command.exitStatus
}
