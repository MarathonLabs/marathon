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
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.io.File

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.lang.SerializationUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Filter
import kotlin.coroutines.CoroutineContext

private const val MAX_CONNECTION_ATTEMPTS = 16

class LocalListSimulatorProvider(override val coroutineContext: CoroutineContext,
                                 private val channel: Channel<DeviceProvider.DeviceEvent>,
                                 private val configuration: IOSConfiguration,
                                 yamlObjectMapper: ObjectMapper,
                                 private val gson: Gson) : SimulatorProvider, HealthChangeListener, CoroutineScope {

    private val logger = MarathonLogging.logger(LocalListSimulatorProvider::class.java.simpleName)

    private val simulators: List<RemoteSimulator>
    private val devices = ConcurrentHashMap<String, IOSDevice>()

    init {
        val file = configuration.devicesFile ?: File(System.getProperty("user.dir"), "Marathondevices")
        val configuredSimulators: List<RemoteSimulator>? = yamlObjectMapper.readValue(file)
        simulators = configuredSimulators ?: emptyList()
    }

    override suspend fun start() = withContext(coroutineContext) {
        logger.info("starts providing ${simulators.count()} simulator devices")
        val jobs = simulators.map {
            async(context = coroutineContext) {
                createDevice(it, RemoteSimulatorConnectionCounter.putAndGet(it.udid))?.let { connect(it) }
            }
        }.joinAll()
    }

    override suspend fun stop() = withContext(coroutineContext) {
        logger.info("stops providing anything")
        if (logger.isDebugEnabled) {
            // print out final summary on attempted simulator connections
            printFailingSimulatorSummary()
        }
//        val jobs = devices.values.map {
//            logger.debug("Disposing ${it.udid}")
//            async {
//                dispose(it)
//
//                notifyDisconnected(it)
//            }
//        }
//        jobs.joinAll()
        devices.values.forEach {
            dispose(it)
            logger.debug("Disposed device ${it.udid}")
        }
        devices.clear()
    }

    override suspend fun onDisconnect(device: IOSDevice) = withContext(coroutineContext) {
        launch(context = coroutineContext) {
            try {
                if (devices.remove(device.serialNumber, device)) {
                    dispose(device)

                    notifyDisconnected(device)
                }
            } catch(e: Exception) { logger.debug("Exception removing device $device") }
        }

        if (device.failureReason == DeviceFailureReason.InvalidSimulatorIdentifier) {
            logger.error("device ${device.udid} does not exist on remote host")
        } else if (RemoteSimulatorConnectionCounter.get(device.udid) < MAX_CONNECTION_ATTEMPTS) {
            launch(context = coroutineContext) {
                delay(499)

                try {
                    createDevice(
                        device.simulator,
                        RemoteSimulatorConnectionCounter.putAndGet(device.udid)
                    )?.let {
                        logger.debug("reconnecting to ${it.udid}")
                        connect(it)
                    }
                } catch(e: Exception) { logger.debug("Exception reconnecting device $device") }
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
    private fun createDevice(simulator: RemoteSimulator, connectionAttempt: Int): IOSDevice? = try {
        IOSDevice(
            simulator = simulator,
            connectionAttempt = connectionAttempt,
            configuration = configuration,
            gson = gson,
            healthChangeListener = this
        )
    } catch (e: DeviceFailureException) {
        logger.error("Failed to initialize ${simulator.udid}-$connectionAttempt with reason ${e.reason}: ${e.message}")
        null
    }

    private fun printFailingSimulatorSummary() {
        simulators
            .map { "${it.udid}@${it.host}" to (RemoteSimulatorConnectionCounter.get(it.udid) - 1) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .forEach {
                logger.debug(String.format("%3d %s", it.second, it.first))
            }
    }
}
