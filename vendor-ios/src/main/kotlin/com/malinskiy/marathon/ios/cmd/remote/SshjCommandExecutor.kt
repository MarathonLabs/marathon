package com.malinskiy.marathon.ios.cmd.remote

import ch.qos.logback.classic.Level
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.LoggerFactory
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.transport.TransportException
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.util.concurrent.TimeoutException
import kotlin.coroutines.CoroutineContext

private const val DEFAULT_PORT = 22
private const val SLEEP_DURATION_MILLIS = 15L

class SshjCommandExecutor(deviceContext: CoroutineContext,
                          val hostAddress: InetAddress,
                          val remoteUsername: String,
                          val remotePrivateKey: File,
                          val port: Int = DEFAULT_PORT,
                          val knownHostsPath: File? = null,
                          private val timeoutMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS,
                          verbose: Boolean = false) : CommandExecutor, CoroutineScope {

    override val coroutineContext: CoroutineContext = deviceContext
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

    override suspend fun exec(command: String, testOutputTimeoutMillis: Long, onLine: (String) -> Unit): Int? {
        val session = startSession(command, timeoutMillis)
        val startTime = System.currentTimeMillis()
        try {
            val executionTimeout = if (timeoutMillis == 0L) Long.MAX_VALUE else timeoutMillis

            val timeoutHandler = SshjCommandOutputTimeoutHandlerImpl(
                testOutputTimeoutMillis,
                SLEEP_DURATION_MILLIS
            )

            val isSessionReadable = { session.isOpen and !session.isEOF }

            withTimeout(executionTimeout) {
                awaitAll(
                    async {
                        readLines(session.inputStream,
                            isSessionReadable,
                            {
                                onLine(it)
                                timeoutHandler.update()
                            }
                        )
                    },
                    async {
                        readLines(session.errorStream,
                            isSessionReadable,
                            {
                                onLine(it)
                                timeoutHandler.update()
                            }
                        )
                    },
                    async {
                        while (isActive and isSessionReadable()) {
                            if (timeoutHandler.getIsUnresponsiveAndWait()) {
                                throw SshjCommandUnresponsiveException("Remote command \n\u001b[1m$command\u001b[0mdid not send any output over ${testOutputTimeoutMillis}ms")
                            }
                        }
                    }
                )
            }
        } catch (e: TimeoutCancellationException) {
            try {
                session.kill()
            } catch (e: TransportException) {}

            throw TimeoutException(e.message)
        } finally {
            try {
                session.close()
            } catch (e: IOException) {
            } catch (e: TransportException) {
            } catch (e: ConnectionException) {
            }
        }
        logger.debug("Execution complete after ${System.currentTimeMillis() - startTime}ms")
        return session.exitStatus
    }

    private suspend fun readLines(inputStream: InputStream, canRead: () -> Boolean, onLine: (String) -> Unit) {
        SshjCommandOutputLineBuffer(onLine).use { lineBuffer ->
            val byteArray = ByteArray(16384)
            val startTime = System.currentTimeMillis()
            while (isActive) {
                val available = inputStream.available()
                // available value is expected to indicate an estimated number of bytes
                // that can be read without blocking (actual count may be smaller).
                //
                // when requesting a zero length, reading from sshj's [ChannelInputStream]
                // blocks. to accurately handle no output timeout, check if session has
                // received EOF.
                //
                val count = when {
                    available > 0 -> inputStream.read(byteArray, 0, available)
                    else -> 0
                }
                // if there was nothing to read
                if (count == 0) {
                    // if session received EOF or has been closed, reading stops
                    if (!canRead()) {
                        logger.trace("Remote command output completed")
                        break
                    }
                    // sleep for a moment
                    delay(SLEEP_DURATION_MILLIS)
                } else {
                    lineBuffer.append(byteArray, count)
                }

                // immediately send any full lines for parsing
                lineBuffer.flush()

                // make sure total execution time isn't too much
                // TODO: this is a duplicate timeout handler, already covered by withTimeout() use
                if (timeoutMillis > 0 && System.currentTimeMillis() - startTime > timeoutMillis) {
                    throw TimeoutException("Remote command execution timeout after ${timeoutMillis}ms")
                }
            }
        }
    }

    override fun exec(command: String, testOutputTimeoutMillis: Long): CommandResult {
        val lines = arrayListOf<String>()
        val exitStatus =
                runBlocking() {
                    try {
                        exec(command, testOutputTimeoutMillis) { lines.add(it) }
                    } catch (e: TimeoutCancellationException) {
                        throw TimeoutException(e.message)
                    }
                }
        return CommandResult(lines.joinToString("\n"), "", exitStatus ?: 1)
    }
}
