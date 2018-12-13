package com.malinskiy.marathon.ios.cmd.remote

import ch.qos.logback.classic.Level
import com.malinskiy.marathon.log.MarathonLogging
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.common.LoggerFactory
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.TransportException
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

private const val DEFAULT_PORT = 22

class SshjCommandExecutor(val hostAddress: InetAddress,
                          val remoteUsername: String,
                          val remotePrivateKey: File,
                          val port: Int = DEFAULT_PORT,
                          val knownHostsPath: File?,
                          verbose: Boolean = false) : CommandExecutor {

    val ssh: SSHClient

    init {
        val config = DefaultConfig()
        val loggerFactory = object : LoggerFactory {
            override fun getLogger(clazz: Class<*>?): Logger = MarathonLogging.logger(
                    name = clazz?.simpleName ?: SshjCommandExecutor::class.java.simpleName,
                    level = if (verbose) { Level.DEBUG } else { Level.ERROR }
            )

            override fun getLogger(name: String?): Logger = MarathonLogging.logger(
                    name = name ?: "",
                    level = if (verbose) { Level.DEBUG } else { Level.ERROR }
            )
        }
        config.loggerFactory = loggerFactory

        ssh = SSHClient(config)
        knownHostsPath?.let { ssh.loadKnownHosts(it) }
        ssh.loadKnownHosts()
        val keys = ssh.loadKeys(remotePrivateKey.path)
        ssh.connect(hostAddress, port)
        ssh.authPublickey(remoteUsername, keys)
    }

    override fun startSession() = ssh.startSession()

    override fun exec(command: String, timeout: Long): CommandResult {
        val session = ssh.startSession()
        var sshCommand: Session.Command? = null
        var stdout: String? = null
        var stderr: String? = null
        try {
            sshCommand = session.exec(command)

            stdout = IOUtils.readFully(sshCommand.inputStream, ssh.transport.config.loggerFactory)
                    .toString("UTF-8")
            stderr = IOUtils.readFully(sshCommand.errorStream, ssh.transport.config.loggerFactory)
                    .toString("UTF-8")

            sshCommand.join(timeout, TimeUnit.SECONDS)
        } catch(e: ConnectionException) {
            ssh.transport.config.loggerFactory.
                    getLogger(SshjCommandExecutor::class.java).
                    debug("${this} exception when executing command ${command} ${e}")
        } catch(e: TransportException) {
            ssh.transport.config.loggerFactory.
                    getLogger(SshjCommandExecutor::class.java).
                    debug("${this} exception when executing command ${command} ${e}")
        } finally {
            try {
                session?.close()
            } catch (e: IOException) { }
        }

        return CommandResult(
                stdout = stdout ?: "",
                stderr = stderr ?: "",
                exitStatus = sshCommand?.exitStatus ?: 1
        )
    }

    override fun disconnect() = ssh.disconnect()
}
