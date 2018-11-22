package com.malinskiy.marathon.ios.cmd.remote

import ch.qos.logback.classic.Level
import com.malinskiy.marathon.log.MarathonLogging
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.LoggerFactory
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.transport.TransportException
import org.slf4j.Logger
import java.io.File
import java.net.InetAddress
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

private const val DEFAULT_PORT = 22

class SshjCommandExecutor(val hostAddress: InetAddress,
                          val remoteUsername: String,
                          val remotePrivateKey: File,
                          val port: Int = DEFAULT_PORT,
                          val knownHostsPath: File?,
                          verbose: Boolean = false) : CommandExecutor {

    private val ssh: SSHClient
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

    override fun startSession(command: String, timeoutMillis: Long): CommandSession {
        return SshjCommandSession(
            executableLine = command,
            ssh = ssh,
            timeoutMillis = timeoutMillis
        )
    }

    override fun exec(command: String, timeoutMillis: Long): CommandResult {
        val session = startSession(command, timeoutMillis)
        session.connect(timeoutMillis)
        val outputCollector = object {
            var stdout = arrayListOf<String>()
            var stderr = arrayListOf<String>()
            fun onStdout(line: String) = stdout.add(line)
            fun onStderr(line: String) = stderr.add(line)
        }
        thread {
            try {
                session.inputStream.reader().forEachLine { outputCollector.onStdout(it) }
            } catch (e: TransportException) { }
        }
        thread {
            try {
                session.errorStream.reader().forEachLine { outputCollector.onStderr(it) }
            } catch (e: TransportException) { }
        }
        try {
            session.use { it.join(timeoutMillis) }
        } catch (e: ConnectionException) {
            if (e.cause is TimeoutException) {
                ssh.logger.debug("Execution timeout expired")
            }
        }

        return CommandResult(
                stdout = outputCollector.stdout.joinToString("\n"),
                stderr = outputCollector.stderr.joinToString("\n"),
                exitStatus = session.exitStatus ?: 1
        )
    }

    override fun disconnect() {
        if (ssh.isConnected) {
            try {
                ssh.disconnect()
            } catch (e: Exception) {
            }
        }

    }
}

private val SSHClient.logger: Logger
    get() { return transport.config.loggerFactory.getLogger(SshjCommandExecutor::class.java) }
