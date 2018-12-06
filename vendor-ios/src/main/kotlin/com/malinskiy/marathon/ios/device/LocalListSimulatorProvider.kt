package com.malinskiy.marathon.ios.device

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.IOSConfiguration
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.atomic.AtomicReference

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class LocalListSimulatorProvider(private val channel: Channel<DeviceProvider.DeviceEvent>,
                                 private val configuration: IOSConfiguration,
                                 yamlObjectMapper: ObjectMapper,
                                 private val gson: Gson) : SimulatorProvider, CoroutineScope {

    private val dispatcher by lazy { newFixedThreadPoolContext(1, "LocalListSimulatorProvider") }
    override val coroutineContext: CoroutineContext
        get() = dispatcher

    private val logger = MarathonLogging.logger(LocalListSimulatorProvider::class.java.simpleName)

    private val devices: MutableList<Device>
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

    private lateinit var healthObserver: Job

    override fun stop() {
        launch {
            healthObserver.cancelAndJoin()

            devices.forEach {
                logger.debug { "Disconnected simulator: ${it.serialNumber}" }

                it.dispose()

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
        healthObserver = launch {
            while (isActive) {
                delay(1000L)

                devices.filterNot { it.healthy }.forEach {
                    logger.debug { "Disconnected unhealthy simulator: ${it.serialNumber}" }

                    it.dispose()

                    channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(it))
                }
                devices.removeIf { !it.healthy }

                if (devices.isEmpty()) {
                    break
                }
            }
        }
    }
}
