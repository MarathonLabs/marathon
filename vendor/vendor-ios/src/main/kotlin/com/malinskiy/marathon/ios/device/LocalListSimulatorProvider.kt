package com.malinskiy.marathon.ios.device

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.IOSConfiguration
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.HealthChangeListener
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
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

private const val MAX_SERIAL = 8

class LocalListSimulatorProvider(private val channel: Channel<DeviceProvider.DeviceEvent>,
                                 private val configuration: IOSConfiguration,
                                 yamlObjectMapper: ObjectMapper,
                                 private val gson: Gson) : SimulatorProvider, HealthChangeListener, CoroutineScope {

    private val dispatcher = newFixedThreadPoolContext(1, "LocalListSimulatorProvider")
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
        launch(context = coroutineContext) {
            logger.info("starts providing ${simulators.count()} simulator devices")
            simulators
                    .mapNotNull { createDevice(it, RemoteSimulatorSerialCounter.putAndGet(it.udid)) }
                    .also {  logger.debug("created ${it.count()} devices") }
                    .forEach { connect(it) }
        }
    }

    override suspend fun stop() = withContext(coroutineContext) {
        logger.info("stops providing anything")
        simulators
                .groupBy { it.host }
                .forEach { (host, simulators) ->
                    logger.debug(host)
                    simulators
                            .map { it.udid to RemoteSimulatorSerialCounter.get(it.udid) }
                            .sortedBy { it.second }
                            .forEach { logger.debug("   - ${it.second}x${it.first}") }
                    simulators.fold(0) { count, simulator ->
                        count + RemoteSimulatorSerialCounter.get(simulator.udid)
                    }.also {
                        logger.debug("   ∑ ${it}")
                    }
                }
        devices.entries
                .filter { devices.remove(it.key, it.value) }
                .forEach { (_, device) ->
                    dispose(device)

                    notifyDisconnected(device)
                }
    }

    override suspend fun onDisconnect(device: IOSDevice) {
        launch(context = coroutineContext) {
            if (devices.remove(device.serialNumber, device)) {
                dispose(device)

                notifyDisconnected(device)
            }
        }

        if (device.failureReason == DeviceFailureReason.MissingDestination) {
            logger.error("device ${device.udid} does not exist on remote host")
        } else if (RemoteSimulatorSerialCounter.get(device.udid) < MAX_SERIAL) {
            launch(context = coroutineContext) {
                delay(499)

                createDevice(
                    device.simulator,
                    RemoteSimulatorSerialCounter.putAndGet(device.udid)
                )?.let {
                    logger.debug("reconnecting to ${it.udid}")
                    connect(it)
                }
            }
        }
    }

    private fun dispose(device: IOSDevice) {
        device.dispose()
    }

    private fun connect(device: IOSDevice) {
        devices.put(device.serialNumber, device)
                ?.let {
                    logger.error("replaced existing device $it with new $device.")
                    dispose(it)
                }
        logger.debug { "discovered device ${device.serialNumber}" }
        notifyConnected(device)
    }

    private fun notifyConnected(device: IOSDevice) = launch(context = coroutineContext) {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(device))
    }

    private fun notifyDisconnected(device: IOSDevice) = launch(context = coroutineContext) {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(device))
    }

    // occassionaly, device constructor would throw an exception when remote simctl command
    // fails with message that simulator services to be no longer available
    private fun createDevice(simulator: RemoteSimulator, simulatorSerial: Int): IOSDevice? = try {
        IOSDevice(
            simulator = simulator,
            simulatorSerial = simulatorSerial,
            configuration = configuration,
            gson = gson,
            healthChangeListener = this
        )
    } catch (e: DeviceFailureException) {
        logger.error("Failed to initialize ${simulator.udid}-$simulatorSerial: ${e.message}")
        null
    }
}
