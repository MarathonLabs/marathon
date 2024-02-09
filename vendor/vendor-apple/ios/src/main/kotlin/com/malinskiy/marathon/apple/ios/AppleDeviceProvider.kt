package com.malinskiy.marathon.apple.ios

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.apple.ios.configuration.AppleTarget
import com.malinskiy.marathon.apple.ios.configuration.Marathondevices
import com.malinskiy.marathon.apple.ios.configuration.Transport
import com.malinskiy.marathon.apple.ios.configuration.Worker
import com.malinskiy.marathon.apple.ios.device.AppleSimulatorProvider
import com.malinskiy.marathon.apple.ios.device.SimulatorFactory
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
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
    private val testBundleIdentifier: AppleTestBundleIdentifier,
    private val gson: Gson,
    private val objectMapper: ObjectMapper,
    private val track: Track,
    private val timer: Timer
) : DeviceProvider, CoroutineScope {

    private val dispatcher = newFixedThreadPoolContext(vendorConfiguration.threadingConfiguration.deviceProviderThreads, "AppleDeviceProvider")
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
        val simulatorFactory = SimulatorFactory(configuration, vendorConfiguration, testBundleIdentifier, gson, track, timer)
        simulatorProvider = AppleSimulatorProvider(
            configuration, vendorConfiguration, gson, coroutineContext, deviceInitializationTimeoutMillis, simulatorFactory, hosts
        )
        simulatorProvider.initialize()
    }

    override suspend fun borrow(): Device {
        return simulatorProvider.borrow()
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
