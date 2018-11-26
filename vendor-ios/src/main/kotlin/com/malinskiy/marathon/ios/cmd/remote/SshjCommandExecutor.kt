package com.malinskiy.marathon.ios.cmd.remote

import ch.qos.logback.classic.Level
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withTimeout
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.LoggerFactory
import org.slf4j.Logger
import java.io.BufferedReader
import java.io.File
import java.net.InetAddress

private const val DEFAULT_PORT = 22
private const val CONNECTION_TIMEOUT_MILLIS = 7200L

class SshjCommandExecutor(val hostAddress: InetAddress,
                          val remoteUsername: String,
                          val remotePrivateKey: File,
                          val port: Int = DEFAULT_PORT,
                          val knownHostsPath: File? = null,
                          val timeoutMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                          verbose: Boolean = false) : CommandExecutor {
    private val ssh: SSHClient

    init {
        val config = DefaultConfig()
        val loggerFactory = object : LoggerFactory {
            override fun getLogger(clazz: Class<*>?): Logger = MarathonLogging.logger(
                name = clazz?.simpleName ?: SshjCommandExecutor::class.java.simpleName,
                level = if (verbose) {
                    Level.DEBUG
                } else {
                    Level.ERROR
                }
            )

            override fun getLogger(name: String?): Logger = MarathonLogging.logger(
                name = name ?: "",
                level = if (verbose) {
                    Level.DEBUG
                } else {
                    Level.ERROR
                }
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
        return SshjCommandSession(command, ssh, timeoutMillis)
    }

    override fun disconnect() {
        if (ssh.isConnected) {
            try {
                ssh.disconnect()
            } catch (e: Exception) {
            }
        }
    }

    override fun exec(command: String, testOutputTimeoutMilliss: Long, reader: (String) -> Unit): Int? {
        val session = startSession(command, timeoutMillis)
        val timeout= launch {
            withTimeout(timeoutMillis) {
                while (isActive) {
                    delay(50)
                }
            }
        }
        session.use {
            it.inputStream.bufferedReader().forEachLine(reader)
            println("done reading stdout lines")
        }
        timeout.cancel()

        return session.exitStatus
    }

    override fun exec(command: String, testOutputTimeoutMillis: Long): CommandResult {
        val session = startSession(command, timeoutMillis)
        val (stdout, stderr)= session.use {
            it.inputStream.bufferedReader().use(BufferedReader::readText) to it.errorStream.bufferedReader().use(BufferedReader::readText)
        }
        return CommandResult(stdout, stderr, session.exitStatus ?: 1)
    }
}

private val SSHClient.logger: Logger
    get() { return transport.config.loggerFactory.getLogger(SshjCommandExecutor::class.java) }
