package com.malinskiy.marathon.ios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.ios.configuration.Marathondevices
import com.malinskiy.marathon.ios.configuration.RemoteSimulator
import com.malinskiy.marathon.ios.device.AppleSimulatorProvider
import com.malinskiy.marathon.ios.device.SimulatorFactory
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.lookup.StringLookupFactory
import java.io.File
import kotlin.coroutines.CoroutineContext

class AppleDeviceProvider(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val gson: Gson,
    private val objectMapper: ObjectMapper,
    private val track: Track,
    private val timer: Timer
) : DeviceProvider, CoroutineScope {

    private val dispatcher = newFixedThreadPoolContext(4, "AppleDeviceProvider")
    override val coroutineContext: CoroutineContext
        get() = dispatcher

    private val logger = MarathonLogging.logger(AppleDeviceProvider::class.java.simpleName)

    private val environmentVariableSubstitutor = StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup())

    private lateinit var simulatorProvider: AppleSimulatorProvider

    override val deviceInitializationTimeoutMillis: Long = configuration.deviceInitializationTimeoutMillis

    override suspend fun initialize() {
        logger.debug("Initializing AppleDeviceProvider")
        val file = vendorConfiguration.devicesFile ?: File(System.getProperty("user.dir"), "Marathondevices")
        val devicesWithEnvironmentVariablesReplaced = environmentVariableSubstitutor.replace(file.readText())
        val devices: Marathondevices = objectMapper.readValue(devicesWithEnvironmentVariablesReplaced)
        val localSimulators = devices.local ?: emptyList()
        val remoteSimulators = devices.remote ?: emptyList()
        val simulators = localSimulators + remoteSimulators
        if (simulators.isEmpty()) {
            throw NoDevicesException("No devices found in the ${file.absolutePath}")
        }

        val simulatorFactory = SimulatorFactory(configuration, vendorConfiguration, gson, track, timer)
        simulatorProvider = AppleSimulatorProvider(
            coroutineContext,
            configuration.deviceInitializationTimeoutMillis,
            simulatorFactory,
            remoteSimulators,
            localSimulators,
        )
        simulatorProvider.initialize()
    }

    override suspend fun terminate() {
        withContext(NonCancellable) {
            logger.debug { "Terminating AppleDeviceProvider" }
            simulatorProvider.terminate()
        }
        dispatcher.close()
    }

    override fun subscribe() = simulatorProvider.subscribe()
}
