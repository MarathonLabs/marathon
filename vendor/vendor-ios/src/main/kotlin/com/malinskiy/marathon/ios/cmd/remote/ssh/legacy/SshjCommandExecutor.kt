//package com.malinskiy.marathon.ios.cmd.remote.ssh.legacy
//
//import ch.qos.logback.classic.Level
//import com.malinskiy.marathon.extension.withTimeout
//import com.malinskiy.marathon.ios.cmd.CommandExecutor
//import com.malinskiy.marathon.ios.cmd.CommandResult
//import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
//import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
//import com.malinskiy.marathon.log.MarathonLogging
//import kotlinx.coroutines.CoroutineName
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.TimeoutCancellationException
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.newSingleThreadContext
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.withContext
//import net.schmizz.keepalive.KeepAlive
//import net.schmizz.keepalive.KeepAliveProvider
//import net.schmizz.sshj.DefaultConfig
//import net.schmizz.sshj.SSHClient
//import net.schmizz.sshj.common.LoggerFactory
//import net.schmizz.sshj.connection.ConnectionException
//import net.schmizz.sshj.connection.ConnectionImpl
//import net.schmizz.sshj.transport.TransportException
//import org.slf4j.Logger
//import java.io.File
//import java.io.IOException
//import java.io.InputStream
//import java.net.ConnectException
//import java.net.InetAddress
//import java.time.Duration
//import java.util.concurrent.TimeoutException
//import kotlin.coroutines.CoroutineContext
//import kotlin.math.min
//
//
//
//class SshjCommandExecutor(
//    connectionId: String,
//    val hostAddress: InetAddress,
//    val remoteUsername: String,
//    val remotePrivateKey: File,
//    val port: Int,
//    val knownHostsPath: File? = null,
//    keepAliveIntervalMillis: Long = 0L,
//) : CommandExecutor, CoroutineScope {
//
//
//    private suspend fun exec(
//        inContext: CoroutineContext,
//        command: String,
//        timeout: Duration,
//        idleTimeout: Duration,
//        onLine: (String) -> Unit
//    ): Int? = withContext(context = inContext) {
//        try {
//            val executionTimeout = if (timeout.isZero) Duration.ofMillis(Long.MAX_VALUE) else timeout
//            withTimeout(executionTimeout) {
//
//                val timeoutWaiter = SshjCommandOutputWaiterImpl(idleTimeout, SLEEP_DURATION_MILLIS)
//                val isSessionReadable = { session.isOpen and !session.isEOF }
//
//                awaitAll(
//                    async(CoroutineName("stdout reader")) {
//                        readLines(
//                            session.inputStream,
//                            isSessionReadable
//                        ) {
//                            timeoutWaiter.update()
//                            onLine(it)
//                        }
//                    },
//                    async(CoroutineName("stderr reader")) {
//                        readLines(
//                            session.errorStream,
//                            isSessionReadable
//                        ) {
//                            timeoutWaiter.update()
//                            onLine(it)
//                        }
//                    },
//                    async(CoroutineName("Timeout waiter")) {
//                        while (isActive and isSessionReadable()) {
//                            if (timeoutWaiter.isExpired) {
//                                throw OutputTimeoutException()
//                            }
//                            timeoutWaiter.wait()
//                        }
//                    }
//                )
//            }
//        } catch (e: TimeoutCancellationException) {
//            try {
//                session.kill()
//            } catch (e: TransportException) {
//            }
//
//            throw TimeoutException(e.message)
//        } catch (e: OutputTimeoutException) {
//            try {
//                session.kill()
//            } catch (e: TransportException) {
//            }
//
//            throw SshjCommandUnresponsiveException("Remote command \n\u001b[1m$command\u001b[0mdid not send any output over ${idleTimeout.toMillis()}ms")
//        } finally {
//            try {
//                session.close()
//            } catch (e: IOException) {
//            } catch (e: TransportException) {
//            } catch (e: ConnectionException) {
//            }
//        }
//        logger.trace("Execution completed after ${System.currentTimeMillis() - startTime}ms")
//        session.exitStatus
//    }
//
//    
//}
