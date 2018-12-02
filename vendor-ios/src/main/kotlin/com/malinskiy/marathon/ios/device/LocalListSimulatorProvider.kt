package com.malinskiy.marathon.ios.device

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.IOSConfiguration
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandExecutor
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.*
import java.io.File
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference

import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext

class LocalListSimulatorProvider(private val channel: Channel<DeviceProvider.DeviceEvent>,
                                 private val configuration: IOSConfiguration,
                                 yamlObjectMapper: ObjectMapper,
                                 private val gson: Gson) : SimulatorProvider, CoroutineScope {

    override val coroutineContext: CoroutineContext by lazy { newFixedThreadPoolContext(1, "LocalListSimulatorProvider") }

    private val logger = MarathonLogging.logger(LocalListSimulatorProvider::class.java.simpleName)

    private val devices: List<Device>
    init {
        val file = configuration.devicesFile ?: File(System.getProperty("user.dir"), "Marathondevices")
        val simulators: List<RemoteSimulator>? = yamlObjectMapper.readValue(file)
        devices = simulators?.map {
            val deviceContext = newFixedThreadPoolContext(1, it.udid)
            IOSDevice(deviceContext = deviceContext,
                    udid = it.udid,
                    hostCommandExecutor = SshjCommandExecutor(
                            coroutineContext = deviceContext,
                            hostAddress = InetAddress.getByName(it.host),
                            remoteUsername = it.username ?: configuration.remoteUsername,
                            remotePrivateKey = configuration.remotePrivateKey,
                            knownHostsPath = configuration.knownHostsPath,
                            verbose = configuration.debugSsh),
                    gson = gson)
        }
        ?: emptyList()
    }

    private lateinit var healthObserver: AtomicReference<Job>

    override fun stop() {
        healthObserver.get().cancel()

        launch {
            devices.forEach {
                logger.debug { "Disconnected simulator: ${it.serialNumber}" }

                it.dispose()

                channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(it))
            }
        }
    }

    override fun start() {
        launch {
            devices.forEach {
                logger.debug { "Found new simulator: ${it.serialNumber}" }

                channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(it))
            }
        }
        val job = launch {
            while (true) {
                delay(1000L)

                devices.filterNot { it.healthy }.forEach {
                    logger.debug { "Disconnected unhealthy simulator: ${it.serialNumber}" }

                    it.dispose()

                    channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(it))
                }
            }
        }
        healthObserver = AtomicReference(job)
    }
}
