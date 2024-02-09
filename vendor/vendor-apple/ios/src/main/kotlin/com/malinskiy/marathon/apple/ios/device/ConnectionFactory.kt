package com.malinskiy.marathon.apple.ios.device

import com.malinskiy.marathon.apple.ios.cmd.CommandExecutor
import com.malinskiy.marathon.apple.ios.cmd.FileBridge
import com.malinskiy.marathon.apple.ios.cmd.local.JvmFileBridge
import com.malinskiy.marathon.apple.ios.cmd.local.KotlinProcessCommandExecutor
import com.malinskiy.marathon.apple.ios.cmd.remote.rsync.RsyncFileBridge
import com.malinskiy.marathon.apple.ios.cmd.remote.rsync.RsyncTarget
import com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj.SshjCommandExecutor
import com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj.SshjCommandExecutorFactory
import com.malinskiy.marathon.apple.ios.configuration.Transport
import com.malinskiy.marathon.apple.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.apple.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.SshAuthentication
import com.malinskiy.marathon.log.MarathonLogging
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Simple implementation of reference counting for closing connections
 */
class ConnectionFactory(private val configuration: Configuration, private val vendorConfiguration: VendorConfiguration.IOSConfiguration) {
    private val logger = MarathonLogging.logger {}
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
        return if (vendorConfiguration.ssh.shareWorkerConnection) {
            Pair(getOrCreateSshCommandExecutor(transport), getOrCreateFileBridge(transport.addr, transport.port, transport.authentication))
        } else {
            Pair(createRemoteCommandExecutor(transport), getOrCreateFileBridge(transport.addr, transport.port, transport.authentication))
        }
    }

    private fun createRemoteCommandExecutor(transport: Transport.Ssh): SshjCommandExecutor? {
        return try {
            val hostAddress = transport.toInetAddressOrNull() ?: throw DeviceFailureException(DeviceFailureReason.UnreachableHost)
            val connectionId = "${hostAddress.hostAddress}:${transport.port}"
            val authConfig = transport.authentication ?: vendorConfiguration.ssh.authentication
            val sshAuthentication = when (authConfig) {
                is SshAuthentication.PasswordAuthentication -> com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj.auth.SshAuthentication.PasswordAuthentication(
                    authConfig.username,
                    authConfig.password
                )

                is SshAuthentication.PublicKeyAuthentication -> com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj.auth.SshAuthentication.PublicKeyAuthentication(
                    authConfig.username,
                    authConfig.key
                )

                null -> throw ConfigurationException("no ssh auth provided for ${transport.addr}:${transport.port}")
            }
            val hostKeyVerifier: HostKeyVerifier = vendorConfiguration.ssh.knownHostsPath?.let {
                OpenSSHKnownHosts(it)
            } ?: PromiscuousVerifier()
            return try {
                sshFactory.connect(
                    addr = transport.addr,
                    port = transport.port,
                    authentication = sshAuthentication,
                    hostKeyVerifier = hostKeyVerifier,
                    debug = vendorConfiguration.ssh.debug,
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
                    address.isReachable(vendorConfiguration.timeoutConfiguration.reachability.toMillis().toInt())
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
    private fun getOrCreateFileBridge(addr: String, port: Int, authentication: SshAuthentication?): FileBridge {
        synchronized(fileBridges) {
            val rsyncSshTarget = RsyncTarget(addr, port)
            return fileBridges.getOrElse(rsyncSshTarget) {
                val defaultBridge = RsyncFileBridge(
                    rsyncSshTarget,
                    configuration,
                    vendorConfiguration,
                    authentication ?: vendorConfiguration.ssh.authentication
                )
                fileBridges[rsyncSshTarget] = defaultBridge
                defaultBridge
            }
        }
    }
}
