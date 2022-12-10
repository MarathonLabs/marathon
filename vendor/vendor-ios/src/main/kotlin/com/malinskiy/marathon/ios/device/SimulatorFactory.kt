package com.malinskiy.marathon.ios.device

import com.google.gson.Gson
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.AppleSimulatorDevice
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.rsync.RsyncFileBridge
import com.malinskiy.marathon.ios.cmd.remote.rsync.RsyncTarget
import com.malinskiy.marathon.ios.configuration.RemoteSimulator
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.simctl.Simctl
import com.malinskiy.marathon.log.MarathonLogging
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

class SimulatorFactory(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val gson: Gson,
) {
    private val logger = MarathonLogging.logger {}
    private val fileManager = FileManager(configuration.outputConfiguration.maxPath, configuration.outputDir)

    suspend fun createRemote(simulator: RemoteSimulator, connectionAttempt: Int): AppleSimulatorDevice? {
        // occasionally would throw an exception when remote simctl command
        // fails with message that simulator services to be no longer available
        return try {
            val hostAddress = simulator.addr.toInetAddressOrNull() ?: throw DeviceFailureException(DeviceFailureReason.UnreachableHost)
            val connectionId = "${simulator.udid}@${hostAddress.hostAddress}"
            val hostCommandExecutor = try {
                SshjCommandExecutor(
                    connectionId = connectionId,
                    hostAddress = hostAddress,
                    remoteUsername = simulator.username ?: vendorConfiguration.remoteUsername,
                    remotePrivateKey = vendorConfiguration.remotePrivateKey,
                    port = simulator.port,
                    knownHostsPath = vendorConfiguration.knownHostsPath,
                    verbose = vendorConfiguration.debugSsh
                )
            } catch (e: DeviceFailureException) {
                throw e
            }

            val simctl = Simctl(hostCommandExecutor, vendorConfiguration.timeoutConfiguration, gson)
            val simctlDevice = try {
                simctl.list().find { it.udid == simulator.udid }
            } catch (e: DeviceFailureException) {
                hostCommandExecutor.close()
                throw e
            } ?: throw DeviceFailureException(DeviceFailureReason.InvalidSimulatorIdentifier)

            val rsyncSshTarget = RsyncTarget(hostAddress.hostAddress, simulator.port)
            val fileBridge = RsyncFileBridge(rsyncSshTarget, configuration, vendorConfiguration)
            val device = AppleSimulatorDevice(
                simctlDevice.udid,
                simctl,
                fileManager,
                configuration,
                vendorConfiguration,
                hostCommandExecutor,
                fileBridge
            )
            device.setup()
            device
        } catch (e: DeviceFailureException) {
            logger.error("Failed to initialize ${simulator.udid}-$connectionAttempt with reason ${e.reason}: ${e.message}")
            null
        }
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
