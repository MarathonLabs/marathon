package com.malinskiy.marathon.ios.device

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandExecutor
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.net.InetAddress

class LocalListSimulatorProvider(private val channel: Channel<DeviceProvider.DeviceEvent>,
                                 val remoteUsername: String,
                                 val remotePublicKey: File,
                                 val yamlObjectMapper: ObjectMapper,
                                 private val gson: Gson) : SimulatorProvider {

    private val logger = MarathonLogging.logger(LocalListSimulatorProvider::class.java.simpleName)

    override fun start() {
        launch {
            val file = File(System.getProperty("user.dir"), "Marathondevices")
            val simulators: List<RemoteSimulator>? = yamlObjectMapper.readValue(file)

            simulators?.forEach {
                logger.debug { "Found new simulator: ${it.udid}" }

                channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(
                        IOSDevice(udid = it.udid,
                                hostCommandExecutor = SshjCommandExecutor(
                                        hostAddress = InetAddress.getByName(it.host),
                                        remoteUsername = remoteUsername,
                                        remotePublicKey = remotePublicKey),
                                gson = gson)
                ))
            }
        }
    }
}