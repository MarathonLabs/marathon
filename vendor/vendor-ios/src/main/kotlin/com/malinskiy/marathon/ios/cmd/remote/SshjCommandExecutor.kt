package com.malinskiy.marathon.ios.cmd.remote

import ch.qos.logback.classic.Level
import com.malinskiy.marathon.extension.withTimeout
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.schmizz.keepalive.KeepAlive
import net.schmizz.keepalive.KeepAliveProvider
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.LoggerFactory
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.ConnectionImpl
import net.schmizz.sshj.transport.TransportException
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.InetAddress
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

private const val SLEEP_DURATION_MILLIS = 15L

class SshjCommandExecutor(
    connectionId: String,
    val hostAddress: InetAddress,
    val remoteUsername: String,
    val remotePrivateKey: File,
    val port: Int,
    val knownHostsPath: File? = null,
    keepAliveIntervalMillis: Long = 0L,
    verbose: Boolean = false
) : CommandExecutor, CoroutineScope {

    override val workerId: String = hostAddress.hostAddress

    private val dispatcher = newSingleThreadContext("$connectionId-ssh")
    override val coroutineContext: CoroutineContext
        get() = dispatcher
    private val ssh: SSHClient

    init {
        val config = DefaultConfig()
        val loggerFactory = object : LoggerFactory {
            override fun getLogger(clazz: Class<*>?): Logger {
                val name = clazz?.simpleName ?: SshjCommandExecutor::class.java.simpleName
                return MarathonLogging.logger(
                    name = name,
                    level = if (verbose) {
                        Level.DEBUG
                    } else {
                        Level.ERROR
                    }
                )
            }

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
        if (keepAliveIntervalMillis > 0) {
            config.keepAliveProvider = object : KeepAliveProvider() {
                override fun provide(connection: ConnectionImpl): KeepAlive {
                    return SshjCommandKeepAlive(connection, hostAddress.toString())
                }
            };
        }

        try {
            ssh = SSHClient(config)
            if (keepAliveIntervalMillis > 0) {
                ssh.connection.keepAlive.keepAliveInterval = (keepAliveIntervalMillis / 1000).toInt()
            }
            knownHostsPath?.let { ssh.loadKnownHosts(it) }
            ssh.loadKnownHosts()
            val keys = ssh.loadKeys(remotePrivateKey.path)
            ssh.connect(hostAddress, port)
            ssh.authPublickey(remoteUsername, keys)
        } catch (e: TransportException) {
            throw DeviceFailureException(DeviceFailureReason.Unknown, e)
        } catch (e: ConnectException) {
            throw DeviceFailureException(DeviceFailureReason.Unknown, e)
        }
    }

    private val logger by lazy {
        MarathonLogging.logger(SshjCommandExecutor::class.java.simpleName)
    }

    override fun startSession(command: String): CommandSession = SshjCommandSession(command, ssh)
    
    override fun close() {
        dispatcher.use {
            try {
                if (ssh.isConnected) {
                    ssh.disconnect()
                }
            } catch (e: IOException) {
                logger.warn("Error disconnecting $e")
            }
        }
    }

    private class OutputTimeoutException : RuntimeException()

    private suspend fun exec(
        inContext: CoroutineContext,
        command: String,
        timeout: Duration,
        idleTimeout: Duration,
        onLine: (String) -> Unit
    ): Int? = withContext(context = inContext) {
        val session = try {
            startSession(command)
        } catch (e: ConnectionException) {
            logger.error("Unable to start a remote shell session $command")
            throw DeviceFailureException(DeviceFailureReason.Unknown, e)
        } catch (e: TransportException) {
            logger.error("Error starting a remote shell session $command")
            throw DeviceFailureException(DeviceFailureReason.Unknown, e)
        }

        val startTime = System.currentTimeMillis()
        logger.trace("Execution starts at ${startTime}ms")
        logger.trace(command)

        try {
            val executionTimeout = if (timeout.isZero) Duration.ofMillis(Long.MAX_VALUE) else timeout
            withTimeout(executionTimeout) {

                val timeoutWaiter = SshjCommandOutputWaiterImpl(idleTimeout, SLEEP_DURATION_MILLIS)
                val isSessionReadable = { session.isOpen and !session.isEOF }

                awaitAll(
                    async(CoroutineName("stdout reader")) {
                        readLines(
                            session.inputStream,
                            isSessionReadable
                        ) {
                            timeoutWaiter.update()
                            onLine(it)
                        }
                    },
                    async(CoroutineName("stderr reader")) {
                        readLines(
                            session.errorStream,
                            isSessionReadable
                        ) {
                            timeoutWaiter.update()
                            onLine(it)
                        }
                    },
                    async(CoroutineName("Timeout waiter")) {
                        while (isActive and isSessionReadable()) {
                            if (timeoutWaiter.isExpired) {
                                throw OutputTimeoutException()
                            }
                            timeoutWaiter.wait()
                        }
                    }
                )
            }
        } catch (e: TimeoutCancellationException) {
            try {
                session.kill()
            } catch (e: TransportException) {
            }

            throw TimeoutException(e.message)
        } catch (e: OutputTimeoutException) {
            try {
                session.kill()
            } catch (e: TransportException) {
            }

            throw SshjCommandUnresponsiveException("Remote command \n\u001b[1m$command\u001b[0mdid not send any output over ${idleTimeout.toMillis()}ms")
        } finally {
            try {
                session.close()
            } catch (e: IOException) {
            } catch (e: TransportException) {
            } catch (e: ConnectionException) {
            }
        }
        logger.trace("Execution completed after ${System.currentTimeMillis() - startTime}ms")
        session.exitStatus
    }

    private suspend fun readLines(inputStream: InputStream, canRead: () -> Boolean, onLine: (String) -> Unit) {
        SshjCommandOutputLineBuffer(onLine).use { lineBuffer ->
            val byteArray = ByteArray(16384)
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
                    available > 0 -> inputStream.read(byteArray, 0, min(available, byteArray.size))
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
            }
        }
    }

    override suspend fun execInto(
        command: String,
        timeout: Duration,
        idleTimeout: Duration,
        onLine: (String) -> Unit,
    ): Int? {
        logger.debug { command }
        return exec(
            coroutineContext,
            command,
            timeout,
            idleTimeout,
            onLine
        )
    }

    override fun execBlocking(
        command: String,
        timeout: Duration,
        idleTimeout: Duration,
    ): CommandResult = runBlocking(coroutineContext + CoroutineName("execBlocking")) {
        val lines = arrayListOf<String>()
        val exitStatus = exec(
            coroutineContext,
            command,
            timeout,
            idleTimeout,
        ) { lines.add(it) }
        CommandResult(lines.joinToString("\n"), "", exitStatus ?: 1)
    }
}
