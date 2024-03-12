package com.malinskiy.marathon.apple.device

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.FileBridge
import com.malinskiy.marathon.apple.cmd.local.JvmFileBridge
import com.malinskiy.marathon.apple.cmd.local.KotlinProcessCommandExecutor
import com.malinskiy.marathon.apple.cmd.remote.rsync.RsyncFileBridge
import com.malinskiy.marathon.apple.cmd.remote.rsync.RsyncTarget
import com.malinskiy.marathon.apple.cmd.remote.ssh.sshj.SshjCommandExecutor
import com.malinskiy.marathon.apple.cmd.remote.ssh.sshj.SshjCommandExecutorFactory
import com.malinskiy.marathon.apple.configuration.Transport
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.apple.RsyncConfiguration
import com.malinskiy.marathon.config.vendor.apple.SshConfiguration
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Duration

/**
 * Simple implementation of reference counting for closing connections
 */
class ConnectionFactory(
    private val configuration: Configuration,
    private val sshConfiguration: SshConfiguration,
    private val rsyncConfiguration: RsyncConfiguration,
    private val reachabilityTimeout: Duration
) {
    private val logger = com.malinskiy.marathon.log.MarathonLogging.logger {}
    private val fileBridges = hashMapOf<RsyncTarget, RsyncFileBridge>()
    private val sshCommandExecutors = hashMapOf<Transport.Ssh, SshjCommandExecutor>()
    private val sshFactory = SshjCommandExecutorFactory()

    private val connectionCounter = HashMap<CommandExecutor, Int>()
    private val lock = Object()

    fun create(transport: Transport): Pair<CommandExecutor?, FileBridge> {
        synchronized(lock) {
            val connection = when (transport) {
                Transport.Local -> createLocal()
                is Transport.Ssh -> createRemote(transport)
            }
            val commandExecutor = connection.first
            if (commandExecutor != null) {
                connectionCounter[commandExecutor] = (connectionCounter[commandExecutor] ?: 0) + 1
            }
            return connection
        }
    }

    fun dispose(commandExecutor: CommandExecutor) {
        synchronized(lock) {
            val users = (connectionCounter[commandExecutor] ?: 0) - 1
            connectionCounter[commandExecutor] = users
            if (users == 0) {
                logger.debug { "Disposing of command executor for ${commandExecutor.host}" }
                commandExecutor.close()
            }
        }
    }

    fun createLocal(): Pair<KotlinProcessCommandExecutor, JvmFileBridge> {
        return Pair(KotlinProcessCommandExecutor(), JvmFileBridge())
    }

    fun createRemote(transport: Transport.Ssh): Pair<CommandExecutor?, FileBridge> {
        return if (sshConfiguration.shareWorkerConnection) {
            Pair(getOrCreateSshCommandExecutor(transport), getOrCreateFileBridge(transport.addr, transport.port, transport.authentication))
        } else {
            Pair(createRemoteCommandExecutor(transport), getOrCreateFileBridge(transport.addr, transport.port, transport.authentication))
        }
    }

    private fun createRemoteCommandExecutor(transport: Transport.Ssh): SshjCommandExecutor? {
        return try {
            val hostAddress = transport.toInetAddressOrNull() ?: throw DeviceFailureException(DeviceFailureReason.UnreachableHost)
            val connectionId = "${hostAddress.hostAddress}:${transport.port}"
            val authConfig = transport.authentication ?: sshConfiguration.authentication
            val sshAuthentication = when (authConfig) {
                is com.malinskiy.marathon.config.vendor.apple.SshAuthentication.PasswordAuthentication -> com.malinskiy.marathon.apple.cmd.remote.ssh.sshj.auth.SshAuthentication.PasswordAuthentication(
                    authConfig.username,
                    authConfig.password
                )

                is com.malinskiy.marathon.config.vendor.apple.SshAuthentication.PublicKeyAuthentication -> com.malinskiy.marathon.apple.cmd.remote.ssh.sshj.auth.SshAuthentication.PublicKeyAuthentication(
                    authConfig.username,
                    authConfig.key
                )

                null -> throw com.malinskiy.marathon.config.exceptions.ConfigurationException("no ssh auth provided for ${transport.addr}:${transport.port}")
            }
            val hostKeyVerifier: HostKeyVerifier = sshConfiguration.knownHostsPath?.let {
                OpenSSHKnownHosts(it)
            } ?: PromiscuousVerifier()
            return try {
                sshFactory.connect(
                    addr = transport.addr,
                    port = transport.port,
                    authentication = sshAuthentication,
                    hostKeyVerifier = hostKeyVerifier,
                    debug = sshConfiguration.debug,
                )
            } catch (e: TransportException) {
                throw DeviceFailureException(DeviceFailureReason.UnreachableHost, e)
            } catch (e: ConnectionException) {
                throw DeviceFailureException(DeviceFailureReason.UnreachableHost, e)
            } catch (e: IOException) {
                throw DeviceFailureException(DeviceFailureReason.UnreachableHost, e)
            }

        } catch (e: DeviceFailureException) {
            logger.error(e) { "Failed to initialize connection to ${transport.addr}:${transport.port}" }
            null
        }
    }

    private fun Transport.Ssh.toInetAddressOrNull(): InetAddress? {
        val address = try {
            InetAddress.getByName(this.addr)
        } catch (e: UnknownHostException) {
            logger.error("Error resolving host $this: $e")
            return null
        }
        return if (this.checkReachability) {
            if (try {
                    address.isReachable(reachabilityTimeout.toMillis().toInt())
                } catch (e: IOException) {
                    logger.error("Error checking reachability of $this: $e")
                    false
                }
            ) {
                address
            } else {
                null
            }
        } else {
            address
        }
    }

    private fun getOrCreateSshCommandExecutor(transport: Transport.Ssh): CommandExecutor? {
        synchronized(sshCommandExecutors) {
            return sshCommandExecutors.getOrPut(transport) {
                createRemoteCommandExecutor(transport) ?: return null
            }
        }
    }

    /**
     * Rsync doesn't work in parallel for the same host, so we have to share the same bridge
     */
    private fun getOrCreateFileBridge(
        addr: String,
        port: Int,
        authentication: com.malinskiy.marathon.config.vendor.apple.SshAuthentication?
    ): FileBridge {
        synchronized(fileBridges) {
            val rsyncSshTarget = RsyncTarget(addr, port)
            return fileBridges.getOrElse(rsyncSshTarget) {
                val defaultBridge = RsyncFileBridge(
                    rsyncSshTarget,
                    configuration,
                    sshConfiguration,
                    rsyncConfiguration,
                    authentication ?: sshConfiguration.authentication
                )
                fileBridges[rsyncSshTarget] = defaultBridge
                defaultBridge
            }
        }
    }
}
