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
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.TimeoutException

private const val DEFAULT_PORT = 22
private const val CONNECTION_TIMEOUT_MILLIS = 7200L

private const val SLEEP_DURATION_MILLIS = 25L

class SshjCommandExecutor(val hostAddress: InetAddress,
                          val remoteUsername: String,
                          val remotePrivateKey: File,
                          val port: Int = DEFAULT_PORT,
                          val knownHostsPath: File? = null,
                          private val timeoutMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
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

    private val logger by lazy {
        MarathonLogging.logger(SshjCommandExecutor::class.java.simpleName)
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

    override fun exec(command: String, testOutputTimeoutMillis: Long, onLine: (String) -> Unit): Int? {
        val lineBuffer = SshjCommandOutputLineBuffer(onLine)
        val session = startSession(command, timeoutMillis)

        val byteArray = ByteArray(16384)
        val startTime = System.currentTimeMillis()
        try {
            var timeSinceLastOutputMillis = 0L
            while (true) {

                val available = session.inputStream.available()
                // available value is expected to indicate an estimated number of bytes
                // that can be read without blocking (actually read number may be smaller).
                val count = when {
                    available > 0 -> session.inputStream.read(byteArray, 0, available)
                    else -> 0
                }
                // when there is nothing to read
                if (count == 0) {
                    // exit if session is closed or received EOF
                    if (session.isEOF or !session.isOpen) {
                        logger.trace("Remote command output completed")
                        break
                    }
                    if (testOutputTimeoutMillis > 0) {
                        timeSinceLastOutputMillis +=  SLEEP_DURATION_MILLIS
                        // if there hasn't been any output for too long, stop execution
                        if (timeSinceLastOutputMillis > testOutputTimeoutMillis) {
                            throw SshjCommandUnresponsiveException("Remote command did not send any output over ${testOutputTimeoutMillis}ms")
                        }
                    }
                    // sleep for a moment
                    Thread.sleep(SLEEP_DURATION_MILLIS)
                } else {
                    timeSinceLastOutputMillis = 0

                    lineBuffer.append(byteArray, count)
                }

                // send any received lines immediately for parsing
                lineBuffer.flush()

                // make sure total execution time isn't too much
                if (timeoutMillis > 0 && System.currentTimeMillis() - startTime > timeoutMillis) {
                    throw TimeoutException("Remote command execution timeout after ${timeoutMillis}ms")
                }
            }
        } finally {
            val closingTime = System.currentTimeMillis()
            try {
                session.close()
            } catch (e: IOException) {
            } catch (e: TransportException) {
            } catch (e: ConnectionException) {
            } finally {
                logger.debug("Session has been closed after ${System.currentTimeMillis() - closingTime}ms")
            }

            // write out any leftovers
            lineBuffer.drain()
        }

        return session.exitStatus
    }

    override fun exec(command: String, testOutputTimeoutMillis: Long): CommandResult {
        val lines = arrayListOf<String>()
        val exitStatus = exec(command, testOutputTimeoutMillis) {
            lines.add(it)
        }
        return CommandResult(lines.joinToString("\n"), "", exitStatus ?: 1)
    }
}

