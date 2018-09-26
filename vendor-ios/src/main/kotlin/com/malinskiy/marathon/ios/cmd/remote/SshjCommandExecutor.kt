package com.malinskiy.marathon.ios.cmd.remote

import com.malinskiy.marathon.log.MarathonLogging
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.LoggerFactory
import net.schmizz.sshj.connection.channel.direct.Session
import org.slf4j.Logger
import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeUnit

private const val defaultPort = 22

class SshjCommandExecutor(val hostAddress: InetAddress,
                          val remoteUsername: String,
                          val remotePublicKey: File,
                          val port: Int = defaultPort) : CommandExecutor {

    val ssh: SSHClient

    init {
        val config = DefaultConfig()
        val loggerFactory = object : LoggerFactory {
            override fun getLogger(clazz: Class<*>?): Logger = MarathonLogging.logger(clazz?.simpleName ?: SshjCommandExecutor::class.java.simpleName)
            override fun getLogger(name: String?): Logger = MarathonLogging.logger(name ?: "")
        }
        config.loggerFactory = loggerFactory

        ssh = SSHClient(config)
        ssh.loadKnownHosts()
        val keys = ssh.loadKeys(remotePublicKey.absolutePath)
        ssh.connect(hostAddress, port)
        ssh.authPublickey(remoteUsername, keys)
    }

    override fun startSession() = ssh.startSession()

    override fun exec(command: String, timeout: Long): CommandResult {
        var session: Session? = null
        var sshCommand: Session.Command? = null
        val stdout: String
        val stderr: String
        var exitStatus = 1
        try {
            session = ssh.startSession()
            sshCommand = session.exec(command)

            sshCommand.join(timeout, TimeUnit.SECONDS)

            stdout = sshCommand.inputStream.bufferedReader().lineSequence().fold("") { acc, line -> acc + line }
            stderr = sshCommand.errorStream.bufferedReader().lineSequence().fold("") { acc, line -> acc + line }
        } finally {
            if (session != null) {
                session.close()
            }
            if (sshCommand != null) {
                exitStatus = sshCommand.exitStatus
            }
        }

        return CommandResult(
                stdout = stdout,
                stderr = stderr,
                exitStatus = exitStatus
        )
    }
}
