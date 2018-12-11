package com.malinskiy.marathon.ios.device

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.IOSConfiguration
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class LocalListSimulatorProvider(private val channel: Channel<DeviceProvider.DeviceEvent>,
                                 private val configuration: IOSConfiguration,
                                 yamlObjectMapper: ObjectMapper,
                                 private val gson: Gson) : SimulatorProvider, CoroutineScope {

    private val dispatcher by lazy { newFixedThreadPoolContext(1, "LocalListSimulatorProvider") }
    override val coroutineContext: CoroutineContext
        get() = dispatcher

    private val logger = MarathonLogging.logger(LocalListSimulatorProvider::class.java.simpleName)

    private var healthCheckJob: Job? = null
    private val devices: MutableList<IOSDevice>
    init {
        val file = configuration.devicesFile ?: File(System.getProperty("user.dir"), "Marathondevices")
        val simulators: List<RemoteSimulator>? = yamlObjectMapper.readValue(file)
        devices = (simulators ?: emptyList())
                .map {
                    IOSDevice(simulator = it,
                        configuration = configuration,
                        gson = gson)
                }
                .toMutableList()
    }

    override fun stop() {
        runBlocking {
            healthCheckJob?.cancelAndJoin()

            devices.forEach {
                logger.debug("Disconnecting simulator ${it.serialNumber}")

                it.dispose()

                logger.debug("Disposed simulator ${it.serialNumber}")

                channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(it))
            }
            dispatcher.close()
        }
    }

    override fun start() {
        launch {
            devices.forEach {
                logger.debug { "Found new simulator: ${it.serialNumber}" }

                channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(it))
            }
        }
        healthCheckJob = launch {
            while (isActive) {
                delay(999)
                val unhealthy = devices.filter { !it.healthy }
                devices.removeAll(unhealthy)
                unhealthy.forEach {
                    it.dispose()
                    channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(it))
                }
                val curable = unhealthy.filter { it.sickness != null && it.sickness != DeviceFailureReason.Unknown }
                if (curable.count() > 0) {
                    launch {
                        delay(666)
                        val cured = curable.map {
                            IOSDevice(
                                simulator = it.simulator,
                                configuration = configuration,
                                gson = gson
                            )
                        }
                        cured.forEach { channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(it)) }
                        devices.addAll(cured)
                    }
                }
            }
        }
    }
}
