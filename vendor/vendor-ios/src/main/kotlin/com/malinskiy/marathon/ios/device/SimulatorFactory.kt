package com.malinskiy.marathon.ios.device

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.SshAuthentication
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.AppleSimulatorDevice
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.FileBridge
import com.malinskiy.marathon.ios.cmd.local.JvmFileBridge
import com.malinskiy.marathon.ios.cmd.local.KotlinProcessCommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.rsync.RsyncFileBridge
import com.malinskiy.marathon.ios.cmd.remote.rsync.RsyncTarget
import com.malinskiy.marathon.ios.cmd.remote.ssh.sshj.SshjCommandExecutorFactory
import com.malinskiy.marathon.ios.cmd.remote.ssh.sshj.auth.SshAuthentication.PasswordAuthentication
import com.malinskiy.marathon.ios.cmd.remote.ssh.sshj.auth.SshAuthentication.PublicKeyAuthentication
import com.malinskiy.marathon.ios.configuration.LocalSimulator
import com.malinskiy.marathon.ios.configuration.RemoteSimulator
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.xcrun.Xcrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

class SimulatorFactory(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val gson: Gson,
    private val track: Track,
    private val timer: Timer,
) {
    private val logger = MarathonLogging.logger {}
    private val fileManager = FileManager(configuration.outputConfiguration.maxPath, configuration.outputDir)
    private val sshFactory = SshjCommandExecutorFactory()
    suspend fun createRemote(simulator: RemoteSimulator): AppleSimulatorDevice? {
        // occasionally would throw an exception when remote simctl command
        // fails with message that simulator services to be no longer available
        return try {
            val hostAddress = simulator.addr.toInetAddressOrNull() ?: throw DeviceFailureException(DeviceFailureReason.UnreachableHost)
            val connectionId = "${simulator.udid}@${hostAddress.hostAddress}"
            val authConfig = simulator.authentication ?: vendorConfiguration.ssh.authentication
            val sshAuthentication = when (authConfig) {
                is SshAuthentication.PasswordAuthentication -> PasswordAuthentication(authConfig.username, authConfig.password)
                is SshAuthentication.PublicKeyAuthentication -> PublicKeyAuthentication(authConfig.username, authConfig.key)
                null -> throw ConfigurationException("no ssh auth provided for ${simulator.udid}@${simulator.addr}:${simulator.port}")
            }
            val hostKeyVerifier: HostKeyVerifier = vendorConfiguration.ssh.knownHostsPath?.let {
                OpenSSHKnownHosts(it)
            } ?: PromiscuousVerifier()
            val commandExecutor = try {
                sshFactory.connect(
                    addr = simulator.addr,
                    port = simulator.port,
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
            
            val fileBridge = getOrCreateFileBridge(hostAddress.hostAddress, simulator.port)

            createSimulator(commandExecutor, fileBridge, simulator.udid)
        } catch (e: DeviceFailureException) {
            logger.error(e) { "Failed to initialize ${simulator.udid}" }
            null
        }
    }

    private val fileBridges = hashMapOf<RsyncTarget, RsyncFileBridge>()

    /**
     * Rsync doesn't work in parallel for the same host, so we have to share the same bridge
     */
    private fun getOrCreateFileBridge(addr: String, port: Int): FileBridge {
        synchronized(fileBridges) {
            val rsyncSshTarget = RsyncTarget(addr, port)
            return fileBridges.getOrElse(rsyncSshTarget) {
                val defaultBridge = RsyncFileBridge(rsyncSshTarget, configuration, vendorConfiguration)
                fileBridges[rsyncSshTarget] = defaultBridge
                defaultBridge
            }
        }
    }

    suspend fun createLocal(simulator: LocalSimulator): AppleSimulatorDevice {
        val commandExecutor = KotlinProcessCommandExecutor()
        val fileBridge = JvmFileBridge()
        return createSimulator(commandExecutor, fileBridge, simulator.udid)
    }

    private suspend fun createSimulator(
        commandExecutor: CommandExecutor,
        fileBridge: FileBridge,
        udid: String,
    ): AppleSimulatorDevice {
        val xcrun = Xcrun(commandExecutor, configuration, vendorConfiguration, gson)
        val simctlDevice = try {
            val simctlDevices = xcrun.simctl.device.list()
            simctlDevices.find { it.udid == udid }?.apply {
                if (isAvailable == false) {
                    throw DeviceFailureException(DeviceFailureReason.InvalidSimulatorIdentifier, "udid $udid is not available")
                }
            } ?: throw DeviceFailureException(DeviceFailureReason.InvalidSimulatorIdentifier, "udid $udid is missing")
        } catch (e: DeviceFailureException) {
            commandExecutor.close()
            throw e
        }

        val device = AppleSimulatorDevice(
            simctlDevice.udid,
            xcrun,
            fileManager,
            configuration,
            vendorConfiguration,
            commandExecutor,
            fileBridge,
            track,
            timer
        )
        track.trackProviderDevicePreparing(device) {
            device.setup()
        }
        return device
    }

    private fun String.toInetAddressOrNull(): InetAddress? {
        val address = try {
            InetAddress.getByName(this)
        } catch (e: UnknownHostException) {
            logger.error("Error resolving host $this: $e")
            return null
        }
        return if (try {
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
    }
}
