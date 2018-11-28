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

private const val WAIT_TIME = 5L

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

    override fun exec(command: String, testOutputTimeoutMillis: Long, reader: (String) -> Unit): Int? {
        val session = startSession(command, timeoutMillis)

        val startTime = System.currentTimeMillis()

        val lineBuffer = LineBuffer(reader)
        val inputStream = session.inputStream
        val byteArray = ByteArray(16384)

        try {
            var timeToResponseCount = 0L
            while (true) {

                val available = inputStream.available()
                val count = when {
                    available > 0 -> inputStream.read(byteArray, 0, available)
                    !session.isOpen -> -1
                    session.isEOF -> -1
                    else -> 0
                }
                if (count < 0) {
                    logger.debug("Remote output stream sent EOF")
                    break
                } else if (count == 0) {
                    val wait = WAIT_TIME * 5
                    if (testOutputTimeoutMillis > 0) {
                        timeToResponseCount += wait
                        if (timeToResponseCount > testOutputTimeoutMillis) {
                            throw SshjCommandUnresponsiveException("command execution timeout after ${testOutputTimeoutMillis}ms")
                        }
                    }
                    Thread.sleep(wait)
                } else {
                    timeToResponseCount = 0

                    lineBuffer.append(byteArray, count)
                }

                lineBuffer.flush()

                if (timeoutMillis > 0 && System.currentTimeMillis() - startTime > timeoutMillis) {
                    throw TimeoutException("command execution timeout after ${timeoutMillis}ms")
                }
            }
        } finally {
            val closingTime = System.currentTimeMillis()
            try {
                session.close()
            } catch (e: IOException) {
            } catch (e: TransportException) {
            } catch (e: ConnectionException) {
            }

            lineBuffer.flush()
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

private class LineBuffer(private val receiver: (String) -> Unit) {
    private val stringBuffer = StringBuffer(16384)

    fun append(bytes: ByteArray, count: Int) {
        synchronized(stringBuffer) {
            stringBuffer.append(String(bytes, 0, count))
        }
    }

    fun flush() {
        val lines = arrayListOf<String>()
        synchronized(stringBuffer) {
            stringBuffer.normalizeEOL()
            while (stringBuffer.isNotEmpty() && stringBuffer.contains('\n')) {
                val line = stringBuffer.takeWhile { it != '\n' }
                stringBuffer.deleteWhile { it != '\n' }.deleteCharAt(0)
                lines.add(line.toString())
            }
        }
        lines.forEach(receiver)
    }
}

private fun StringBuffer.deleteWhile(predicate: (Char) -> Boolean): StringBuffer {
    while (this.isNotEmpty() && predicate(this.first())) {
        this.deleteCharAt(0)
    }
    return this
}

// replaces both \r and \r\n with a single \n
private fun StringBuffer.normalizeEOL(): StringBuffer {
    var index = 0
    while (index < length) {
        if (index + 1 < length && get(index) == '\r' && get(index+1) == '\n') {
            deleteCharAt(index)
        } else {
            if (get(index) == '\r') {
                setCharAt(index, '\n')
            }
            index ++
        }
    }
    return this
}
