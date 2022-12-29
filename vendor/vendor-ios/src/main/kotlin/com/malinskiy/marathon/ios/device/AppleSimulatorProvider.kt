package com.malinskiy.marathon.ios.device

import com.google.gson.Gson
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.SshAuthentication
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.AppleSimulatorDevice
import com.malinskiy.marathon.ios.bin.AppleBinaryEnvironment
import com.malinskiy.marathon.ios.bin.xcrun.simctl.model.SimctlDevice
import com.malinskiy.marathon.ios.bin.xcrun.simctl.model.SimctlListDevicesOutput
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.FileBridge
import com.malinskiy.marathon.ios.cmd.local.JvmFileBridge
import com.malinskiy.marathon.ios.cmd.local.KotlinProcessCommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.rsync.RsyncFileBridge
import com.malinskiy.marathon.ios.cmd.remote.rsync.RsyncTarget
import com.malinskiy.marathon.ios.cmd.remote.ssh.sshj.SshjCommandExecutorFactory
import com.malinskiy.marathon.ios.cmd.remote.ssh.sshj.auth.SshAuthentication.PasswordAuthentication
import com.malinskiy.marathon.ios.cmd.remote.ssh.sshj.auth.SshAuthentication.PublicKeyAuthentication
import com.malinskiy.marathon.ios.configuration.AppleTarget
import com.malinskiy.marathon.ios.configuration.Transport
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

private const val MAX_CONNECTION_ATTEMPTS = 16

class AppleSimulatorProvider(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val gson: Gson,
    override val coroutineContext: CoroutineContext,
    override val deviceInitializationTimeoutMillis: Long,
    private val simulatorFactory: SimulatorFactory,
    private val hosts: Map<Transport, List<AppleTarget>>,
) : DeviceProvider, CoroutineScope {

    private val logger = MarathonLogging.logger(AppleSimulatorProvider::class.java.simpleName)

    private val job = Job()

    private val devices = ConcurrentHashMap<String, AppleSimulatorDevice>()
    private val sshFactory = SshjCommandExecutorFactory()
    private val fileBridges = hashMapOf<RsyncTarget, RsyncFileBridge>()

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()

    override fun subscribe() = channel

    private val localCommandExecutor = KotlinProcessCommandExecutor()
    private val localFileBridge = JvmFileBridge()

    override suspend fun initialize() = withContext(coroutineContext) {
        logger.debug { "Establishing communication with [${hosts.keys.joinToString()}]" }
        val deferred = hosts.map { (transport, targets) ->
            async {
                when (transport) {
                    Transport.Local -> {
                        val bin = AppleBinaryEnvironment(localCommandExecutor, configuration, vendorConfiguration, gson)

                        val plan = plan(transport, bin, targets)

                        val deferredExisting = createExisting(transport, plan, localCommandExecutor, localFileBridge)
                        val deferredProvisioning = createNew(transport, plan, bin, localCommandExecutor, localFileBridge)
                        (deferredExisting + deferredProvisioning).awaitAll()
                    }

                    is Transport.Ssh -> {
                        val commandExecutor = createRemoteCommandExecutor(transport) ?: return@async
                        val fileBridge = getOrCreateFileBridge(transport.addr, transport.port)
                        val bin = AppleBinaryEnvironment(commandExecutor, configuration, vendorConfiguration, gson)

                        val plan = plan(transport, bin, targets)

                        val deferredExisting = createExisting(transport, plan, commandExecutor, fileBridge)
                        val deferredProvisioning = createNew(transport, plan, bin, commandExecutor, fileBridge)
                        (deferredExisting + deferredProvisioning).awaitAll()
                    }
                }
            }
        }
        awaitAll(*deferred.toTypedArray())
        Unit
    }

    private suspend fun createExisting(transport: Transport, plan: ProvisioningPlan, commandExecutor: CommandExecutor, fileBridge: FileBridge) =
        plan.existingSimulators.map { udid ->
            supervisorScope {
                async {
                    val simulator = createSimulator(commandExecutor, fileBridge, udid)
                    connect(simulator)
                }
            }
        }

    private suspend fun createSimulator(
        commandExecutor: CommandExecutor,
        fileBridge: FileBridge,
        udid: String
    ) = simulatorFactory.create(commandExecutor, fileBridge, udid)

    private suspend fun createNew(
        transport: Transport,
        plan: ProvisioningPlan,
        bin: AppleBinaryEnvironment,
        commandExecutor: CommandExecutor,
        fileBridge: FileBridge,
    ) = plan.needsProvisioning.map { profile ->
        supervisorScope {
            async {
                val simctlListDevicesOutput = bin.xcrun.simctl.device.list()
                if (!verifySimulatorCanBeProvisioned(simctlListDevicesOutput, profile, transport)) {
                    return@async
                }

                val name = "${profile.newNamePrefix}-${UUID.randomUUID()}"
                val deviceTypeId = profile.fullyQualifiedDeviceTypeId
                val udid = bin.xcrun.simctl.simulator.create(
                    name,
                    deviceTypeId,
                    profile.fullyQualifiedRuntimeId
                )
                if (udid != null) {
                    val simulator = createSimulator(commandExecutor, fileBridge, udid)
                    connect(simulator)
                } else {
                    logger.error { "Failed to create simulator for profile $profile" }
                }
            }
        }
    }

    private fun verifySimulatorCanBeProvisioned(
        simctlListDevicesOutput: SimctlListDevicesOutput,
        profile: AppleTarget.SimulatorProfile,
        transport: Transport
    ): Boolean {
        if (!simctlListDevicesOutput.devicetypes.any {
                it.identifier == profile.fullyQualifiedDeviceTypeId
            }) {
            logger.error { "device type ${profile.fullyQualifiedDeviceTypeId} is not available at $transport" }
            return false
        }
        if (profile.fullyQualifiedRuntimeId != null && !simctlListDevicesOutput.runtimes.any {
                it.identifier == profile.fullyQualifiedRuntimeId
            }) {
            logger.error { "runtime ${profile.fullyQualifiedRuntimeId} is not available at $transport" }
            return false
        }
        return true
    }

    /**
     * Per-host provisioning
     */
    suspend fun plan(transport: Transport, bin: AppleBinaryEnvironment, targets: List<AppleTarget>): ProvisioningPlan {
        val simulatorDevices: Map<String, SimctlDevice> = bin.xcrun.simctl.device.listDevices()
            .filter { it.isAvailable ?: false }
            .groupBy { it.udid }
            .mapValues { it.value.first() } //This will fail silently if the same udid is used twice 

        val simulators = mutableListOf<AppleTarget.Simulator>()
        val simulatorProfiles = mutableListOf<AppleTarget.SimulatorProfile>()
        val physical = mutableListOf<AppleTarget.Physical>()
        targets.forEach { device ->
            when (device) {
                is AppleTarget.Simulator -> simulators.add(device)
                is AppleTarget.Physical -> physical.add(device)
                is AppleTarget.SimulatorProfile -> simulatorProfiles.add(device)
            }
        }
        val availableUdids = simulatorDevices.keys
        val usedUdids = mutableSetOf<String>()
        simulators.forEach {
            if (!availableUdids.contains(it.udid)) {
                logger.error { "udid ${it.udid} is not available at $transport" }
            } else {
                usedUdids.add(it.udid)
            }
        }
        val unusedDevices = simulatorDevices.filterKeys { !usedUdids.contains(it) }.toMutableMap()
        val reuseUdid = mutableSetOf<String>()
        val createProfiles = mutableListOf<AppleTarget.SimulatorProfile>()
        simulatorProfiles.forEach { profile ->
            reuseExistingSimulator(unusedDevices - reuseUdid, profile)?.let {
                reuseUdid.add(it) 
                unusedDevices.remove(it)
            } ?: createProfiles.add(profile)
        }

        //Maybe we should sanity-check if these are available
        val physicalUdids = physical.map { it.udid }.toSet()
        
        return ProvisioningPlan(usedUdids + reuseUdid, createProfiles, physicalUdids)
    }

    /**
     * @return udid of reusable simulator or null if nothing matches
     */
    private fun reuseExistingSimulator(
        devices: Map<String, SimctlDevice>,
        profile: AppleTarget.SimulatorProfile,
    ): String? {
        return devices.values.find {
            it.deviceTypeIdentifier == profile.fullyQualifiedDeviceTypeId &&
                (profile.fullyQualifiedRuntimeId?.let { fqri -> fqri == it.runtime } ?: true)
        }?.udid
    }

    fun createRemoteCommandExecutor(transport: Transport.Ssh): CommandExecutor? {
        return try {
            val hostAddress = transport.addr.toInetAddressOrNull() ?: throw DeviceFailureException(DeviceFailureReason.UnreachableHost)
            val connectionId = "${hostAddress.hostAddress}:${transport.port}"
            val authConfig = transport.authentication ?: vendorConfiguration.ssh.authentication
            val sshAuthentication = when (authConfig) {
                is SshAuthentication.PasswordAuthentication -> PasswordAuthentication(
                    authConfig.username,
                    authConfig.password
                )

                is SshAuthentication.PublicKeyAuthentication -> PublicKeyAuthentication(
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

    override suspend fun terminate() = withContext(NonCancellable) {
        logger.info("stops providing anything")
        channel.close()
        if (logger.isDebugEnabled) {
            // print out final summary on attempted simulator connections
//            printFailingSimulatorSummary()
        }
        devices.values.forEach {
            dispose(it)
            logger.debug("Disposed device ${it.udid}")
        }
        devices.clear()
    }

//    suspend fun onDisconnect(device: AppleSimulatorDevice, remoteSimulator: AppleTarget.Simulator, reason: DeviceFailureReason) =
//        withContext(coroutineContext + CoroutineName("onDisconnect")) {
//            launch(context = coroutineContext + job + CoroutineName("disconnector")) {
//                try {
//                    if (devices.remove(device.serialNumber, device)) {
//                        dispose(device)
//                        notifyDisconnected(device)
//                    }
//                } catch (e: Exception) {
//                    logger.debug("Exception removing device ${device.udid}")
//                }
//            }
//
//            if (reason == DeviceFailureReason.InvalidSimulatorIdentifier) {
//                logger.error("device ${device.udid} does not exist on remote host")
//            } else if (RemoteSimulatorConnectionCounter.get(device.udid) < MAX_CONNECTION_ATTEMPTS) {
//                launch(context = coroutineContext + job + CoroutineName("reconnector")) {
//                    delay(499)
//                    RemoteSimulatorConnectionCounter.putAndGet(device.udid)
//                    simulatorFactory.createRemote(remoteSimulator)?.let {
//                        connect(it)
//                    }
//                }
//            }
//        }

    private fun dispose(device: AppleSimulatorDevice) {
        device.dispose()
    }

    private fun connect(device: AppleSimulatorDevice) {
        devices.put(device.serialNumber, device)
            ?.let {
                logger.error("replaced existing device $it with new $device.")
                dispose(it)
            }
        notifyConnected(device)
    }

    private fun notifyConnected(device: AppleSimulatorDevice) = launch(context = coroutineContext) {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(device))
    }

    private fun notifyDisconnected(device: AppleSimulatorDevice) = launch(context = coroutineContext) {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(device))
    }

//    private fun printFailingSimulatorSummary() {
//        simulators
//            .map { "${it.udid}@${it.transport}" to (RemoteSimulatorConnectionCounter.get(it.udid) - 1) }
//            .filter { it.second > 0 }
//            .sortedByDescending { it.second }
//            .forEach {
//                logger.debug(String.format("%3d %s", it.second, it.first))
//            }
//    }
}
