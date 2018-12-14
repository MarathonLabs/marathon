package com.malinskiy.marathon.ios.device

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.IOSConfiguration
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.HealthListener
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.CoroutineScope
import java.io.File

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class LocalListSimulatorProvider(private val channel: Channel<DeviceProvider.DeviceEvent>,
                                 private val configuration: IOSConfiguration,
                                 yamlObjectMapper: ObjectMapper,
                                 private val gson: Gson) : SimulatorProvider, HealthListener, CoroutineScope {

    private val dispatcher by lazy { newFixedThreadPoolContext(1, "LocalListSimulatorProvider") }
    override val coroutineContext: CoroutineContext
        get() = dispatcher

    private val logger = MarathonLogging.logger(LocalListSimulatorProvider::class.java.simpleName)

    private val simulators: List<RemoteSimulator>
    private val devices: MutableList<IOSDevice> = mutableListOf()
    init {
        val file = configuration.devicesFile ?: File(System.getProperty("user.dir"), "Marathondevices")
        val configuredSimulators: List<RemoteSimulator>? = yamlObjectMapper.readValue(file)
        simulators = configuredSimulators ?: emptyList()
    }

    override suspend fun start() = withContext(coroutineContext) {
        simulators.map(this@LocalListSimulatorProvider::createDevice).forEach { connect(it) }
    }

    override suspend fun stop() = withContext(coroutineContext) {
        devices.forEach { disconnect(it) }
    }

    override suspend fun onDisconnect(device: IOSDevice) {
        disconnect(device)

        val simulator = device.simulator
        val clone = createDevice(simulator)

        connect(clone)
    }

    private suspend fun disconnect(device: IOSDevice) {
        devices.remove(device)
        logger.debug("Disconnecting simulator ${device.serialNumber}")
        device.dispose()
        logger.debug("Disposed simulator ${device.serialNumber}")
        channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(device))
    }

    private suspend fun connect(device: IOSDevice) {
        devices.add(device)
        logger.debug { "Found new simulator: ${device.serialNumber}" }
        channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(device))
    }

    private fun createDevice(simulator: RemoteSimulator): IOSDevice = IOSDevice(
        simulator = simulator,
        configuration = configuration,
        gson = gson,
        healthListener = this
    )
}
