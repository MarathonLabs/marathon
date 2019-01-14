package com.malinskiy.marathon.ios.cmd.remote

import ch.qos.logback.classic.Level
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.*
import net.schmizz.concurrent.Promise
import net.schmizz.keepalive.KeepAlive
import net.schmizz.keepalive.KeepAliveProvider
import net.schmizz.keepalive.KeepAliveRunner
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.DisconnectReason
import net.schmizz.sshj.common.LoggerFactory
import net.schmizz.sshj.common.SSHPacket
import net.schmizz.sshj.connection.Connection
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.ConnectionImpl
import net.schmizz.sshj.transport.TransportException
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.RuntimeException
import java.lang.String.format
import java.net.InetAddress
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

private const val DEFAULT_PORT = 22
private const val SLEEP_DURATION_MILLIS = 15L
private const val SSH_SERVER_KEEPALIVE_INTERVAL_MILLIS = 5000

class Keeper(conn: ConnectionImpl, hostname: String) : KeepAlive(conn, "$hostname keep-alive") {

    /** The max number of keep-alives that should be unanswered before killing the connection.  */
    @get:Synchronized
    @set:Synchronized
    var maxAliveCount = 5

    /** The queue of promises.  */
    private val queue = LinkedList<Promise<SSHPacket, ConnectionException>>()

    @Throws(TransportException::class, ConnectionException::class)
    override fun doKeepAlive() {
        // Ensure the service is set... This means that the key exchange is done and the connection is up.
        if (conn == conn.transport.service) {
            emptyQueue(queue)
            checkMaxReached(queue)
            queue.add(conn.sendGlobalRequest("keepalive@openssh.com", true, ByteArray(0)))
        }
    }

    @Throws(ConnectionException::class)
    private fun checkMaxReached(queue: Queue<Promise<SSHPacket, ConnectionException>>) {
        if (queue.size >= maxAliveCount) {
            throw ConnectionException(
                DisconnectReason.CONNECTION_LOST,
                format("Did not receive any keep-alive response for %s seconds", maxAliveCount * keepAliveInterval)
            )
        }
    }

    private fun emptyQueue(queue: Queue<Promise<SSHPacket, ConnectionException>>) {
        var peek: Promise<SSHPacket, ConnectionException>? = queue.peek()
        while (peek != null && peek.isFulfilled) {
            log.debug("Received response from server to our keep-alive.")
            queue.remove()
            peek = queue.peek()
        }
    }
}

class SshjCommandExecutor(deviceContext: CoroutineContext,
                          udid: String,
                          val hostAddress: InetAddress,
                          val remoteUsername: String,
                          val remotePrivateKey: File,
                          val port: Int = DEFAULT_PORT,
                          val knownHostsPath: File? = null,
                          verbose: Boolean = false) : CommandExecutor, CoroutineScope {

    override val coroutineContext: CoroutineContext = newSingleThreadContext("$udid-ssh")
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
        // config.keepAliveProvider = KeepAliveProvider.KEEP_ALIVE
        config.keepAliveProvider = object : KeepAliveProvider() {
            override fun provide(connection: ConnectionImpl): KeepAlive {
                return Keeper(connection, hostAddress.toString())
            }
        };

        ssh = SSHClient(config)
        ssh.connection.keepAlive.keepAliveInterval = SSH_SERVER_KEEPALIVE_INTERVAL_MILLIS / 1000
        knownHostsPath?.let { ssh.loadKnownHosts(it) }
        ssh.loadKnownHosts()
        val keys = ssh.loadKeys(remotePrivateKey.path)
        ssh.connect(hostAddress, port)
        ssh.authPublickey(remoteUsername, keys)
    }

    private val logger by lazy {
        MarathonLogging.logger(SshjCommandExecutor::class.java.simpleName)
    }

    override fun startSession(command: String): CommandSession {
        return SshjCommandSession(command, ssh)
    }

    override fun disconnect() {
        if (ssh.isConnected) {
            try {
                ssh.disconnect()
            } catch (e: IOException) {
                logger.warn("Error disconnecting $e")
            }
        }
    }

    private class OutputTimeoutException: RuntimeException()

    override suspend fun exec(command: String, timeoutMillis: Long, testOutputTimeoutMillis: Long, onLine: (String) -> Unit): Int? {
        val session = try {
            startSession(command)
        } catch (e: ConnectionException) {
            logger.error("Unable to start a remote shell session")
            throw e
        } catch (e: TransportException) {
            logger.error("Unable to start a remote shell session")
            throw e
        }

        val startTime = System.currentTimeMillis()
        logger.trace("Execution starts at ${startTime}ms")
        logger.trace(command)

        try {
            val executionTimeout = if (timeoutMillis == 0L) Long.MAX_VALUE else timeoutMillis
            withTimeout(executionTimeout) {

                val timeoutWaiter = SshjCommandOutputWaiterImpl(testOutputTimeoutMillis, SLEEP_DURATION_MILLIS)
                val isSessionReadable = { session.isOpen and !session.isEOF }

                awaitAll(
                    async(CoroutineName("stdout reader")) {
                        readLines(session.inputStream,
                            isSessionReadable,
                            {
                                timeoutWaiter.update()
                                onLine(it)
                            }
                        )
                    },
                    async(CoroutineName("stderr reader")) {
                        readLines(session.errorStream,
                            isSessionReadable,
                            {
                                timeoutWaiter.update()
                                onLine(it)
                            }
                        )
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

            throw SshjCommandUnresponsiveException("Remote command \n\u001b[1m$command\u001b[0mdid not send any output over ${testOutputTimeoutMillis}ms")
        } finally {
            try {
                session.close()
            } catch (e: IOException) {
            } catch (e: TransportException) {
            } catch (e: ConnectionException) {
            }
        }
        logger.trace("Execution completed after ${System.currentTimeMillis() - startTime}ms")
        return session.exitStatus
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

    override fun exec(command: String, timeoutMillis: Long, testOutputTimeoutMillis: Long): CommandResult {
        val lines = arrayListOf<String>()
        val exitStatus =
                runBlocking(coroutineContext + CoroutineName("blocking exec")) {
                    exec(command, timeoutMillis, testOutputTimeoutMillis) { lines.add(it) }
                }
        return CommandResult(lines.joinToString("\n"), "", exitStatus ?: 1)
    }
}
