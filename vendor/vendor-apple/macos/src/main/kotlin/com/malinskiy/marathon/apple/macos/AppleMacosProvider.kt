package com.malinskiy.marathon.apple.macos

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.apple.AppleTestBundleIdentifier
import com.malinskiy.marathon.apple.bin.AppleBinaryEnvironment
import com.malinskiy.marathon.apple.configuration.AppleTarget
import com.malinskiy.marathon.apple.configuration.Marathondevices
import com.malinskiy.marathon.apple.configuration.Transport
import com.malinskiy.marathon.apple.configuration.Worker
import com.malinskiy.marathon.apple.device.ConnectionFactory
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.lookup.StringLookupFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class AppleMacosProvider(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.MacosConfiguration,
    private val testBundleIdentifier: AppleTestBundleIdentifier,
    private val gson: Gson,
    private val objectMapper: ObjectMapper,
    private val track: Track,
    private val timer: Timer
) : DeviceProvider, CoroutineScope {

    private val logger = MarathonLogging.logger(AppleMacosProvider::class.java.simpleName)

    private val dispatcher =
        newFixedThreadPoolContext(vendorConfiguration.threadingConfiguration.deviceProviderThreads, "AppleDeviceProvider")
    override val coroutineContext: CoroutineContext
        get() = dispatcher
    override val deviceInitializationTimeoutMillis = configuration.deviceInitializationTimeoutMillis

    private val job = Job()

    private val devices = ConcurrentHashMap<String, MacosDevice>()
    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    private val connectionFactory = ConnectionFactory(
        configuration,
        vendorConfiguration.ssh,
        vendorConfiguration.rsync,
        vendorConfiguration.timeoutConfiguration.reachability
    )
    private val environmentVariableSubstitutor = StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup())
    private val fileManager = FileManager(
        configuration.outputConfiguration.maxPath,
        configuration.outputConfiguration.maxFilename,
        configuration.outputDir
    )

    override fun subscribe() = channel

    override suspend fun initialize() = withContext(coroutineContext) {
        logger.debug("Initializing AppleMacosProvider")
        val file = vendorConfiguration.devicesFile ?: File(System.getProperty("user.dir"), "Marathondevices")
        val devicesWithEnvironmentVariablesReplaced = environmentVariableSubstitutor.replace(file.readText())
        val workers: List<Worker> = try {
            objectMapper.readValue<Marathondevices>(devicesWithEnvironmentVariablesReplaced).workers
        } catch (e: JsonMappingException) {
            throw NoDevicesException("Invalid Marathondevices file ${file.absolutePath} format", e)
        }
        if (workers.isEmpty()) {
            throw NoDevicesException("No workers found in the ${file.absolutePath}")
        }
        val hosts: Map<Transport, List<AppleTarget>> = mutableMapOf<Transport, List<AppleTarget>>().apply {
            workers.map {
                put(it.transport, it.devices)
            }
        }

        logger.debug { "Establishing communication with [${hosts.keys.joinToString()}]" }
        val deferred = hosts.filter {
            var use = false
            it.value.forEach { device ->
                when (device) {
                    is AppleTarget.Host -> use = true
                    else -> logger.warn { "macOS vendor runs do not use anything but host device for testing. Skipping" }
                }
            }
            use
        }.map { (transport, _) ->
            async {
                initializeForTransport(transport)
            }
        }
        awaitAll(*deferred.toTypedArray())
        Unit
    }

    override suspend fun borrow(): Device {
        while (devices.isEmpty()) {
            delay(200)
        }
        return devices.values.random()
    }

    private suspend fun initializeForTransport(transport: Transport) {
        val (commandExecutor, fileBridge) = connectionFactory.create(transport)
        if (commandExecutor == null) {
            return
        }
        val bin = AppleBinaryEnvironment(commandExecutor, configuration, vendorConfiguration.timeoutConfiguration, gson)
        var udid = bin.systemProfiler.getProvisioningUdid()
        if (udid.isBlank()) {
            udid = bin.ioreg.getUDID()
        }
        val device = MacosDevice(
            udid,
            transport,
            Sdk.MACOS,
            bin,
            testBundleIdentifier,
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
        connect(transport, device)
    }

    override suspend fun terminate() = withContext(NonCancellable) {
        withContext(NonCancellable) {
            logger.debug { "Terminating ${AppleMacosProvider::class.simpleName}" }
            channel.close()
            if (logger.isDebugEnabled) {
                // print out final summary on attempted simulator connections
                //printFailingSimulatorSummary()
            }
            val deferredDispose = devices.map { (uuid, device) ->
                async {
                    try {
                        dispose(device)
                        connectionFactory.dispose(device.commandExecutor)
                    } catch (e: Exception) {
                        //We don't really care during termination about exceptions
                    }
                    logger.debug("Disposed device ${device.udid}")
                }
            }
            deferredDispose.awaitAll()
            devices.clear()
        }
        dispatcher.close()
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

    private fun dispose(device: MacosDevice) {
        device.dispose()
    }

    private fun connect(transport: Transport, device: MacosDevice) {
        devices.put(device.serialNumber, device)
            ?.let {
                logger.error("replaced existing device $it with new $device.")
                dispose(it)
            }
        notifyConnected(device)
    }

    private fun notifyConnected(device: MacosDevice) = launch(context = coroutineContext) {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(device))
    }

    private fun notifyDisconnected(device: MacosDevice) = launch(context = coroutineContext) {
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
