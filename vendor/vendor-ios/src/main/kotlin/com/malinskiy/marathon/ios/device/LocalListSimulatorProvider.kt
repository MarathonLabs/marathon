package com.malinskiy.marathon.ios.device

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.IOSConfiguration
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.HealthChangeListener
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.CoroutineScope
import java.io.File

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class LocalListSimulatorProvider(private val channel: Channel<DeviceProvider.DeviceEvent>,
                                 private val configuration: IOSConfiguration,
                                 yamlObjectMapper: ObjectMapper,
                                 private val gson: Gson) : SimulatorProvider, HealthChangeListener, CoroutineScope {

    private val dispatcher by lazy { newFixedThreadPoolContext(1, "LocalListSimulatorProvider") }
    override val coroutineContext: CoroutineContext
        get() = dispatcher

    private val logger = MarathonLogging.logger(LocalListSimulatorProvider::class.java.simpleName)

    private val simulators: List<RemoteSimulator>
    private val devices = ConcurrentHashMap<String, IOSDevice>()

    init {
        val file = configuration.devicesFile ?: File(System.getProperty("user.dir"), "Marathondevices")
        val configuredSimulators: List<RemoteSimulator>? = yamlObjectMapper.readValue(file)
        simulators = configuredSimulators ?: emptyList()
    }

    override suspend fun start() {
        launch {
            simulators
                    .map(this@LocalListSimulatorProvider::createDevice)
                    .forEach { connect(it) }
        }
    }

    override suspend fun stop() = withContext(coroutineContext) {
        devices.entries
                .filter { devices.remove(it.key, it.value) }
                .map { it.value }
                .forEach { device ->
                    dispose(device)

                    notifyDisconnected(device)
                }
    }

    override suspend fun onDisconnect(device: IOSDevice) {
        launch {
            if (devices.remove(device.serialNumber, device)) {
                dispose(device)

                notifyDisconnected(device)
            }
        }

        when (device.failureReason) {
            DeviceFailureReason.MissingDestination ->
                logger.info("Device ${device.udid} does not exist")
            else ->
                launch {
                    delay(499)

                    val restartedDevice = createDevice(device.simulator)
                    logger.debug("reconnecting to ${restartedDevice.udid}")
                    connect(restartedDevice)
                }
            }
        }

    private fun dispose(device: IOSDevice) {
        device.dispose()
    }

    private fun connect(device: IOSDevice) {
        devices.put(device.serialNumber, device)
                ?.let {
                    logger.error("Replaced existing device $it with new $device.")
                    dispose(it)
                }

        logger.debug { "Discovered simulator ${device.serialNumber}" }
        notifyConnected(device)
    }

    private fun notifyConnected(device: IOSDevice) = launch {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(device))
    }

    private fun notifyDisconnected(device: IOSDevice) = launch {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(device))
    }

    private fun createDevice(simulator: RemoteSimulator): IOSDevice = IOSDevice(
        simulator = simulator,
        configuration = configuration,
        gson = gson,
        healthChangeListener = this
    )
}
