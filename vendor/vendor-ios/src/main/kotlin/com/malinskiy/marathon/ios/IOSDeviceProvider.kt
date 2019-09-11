package com.malinskiy.marathon.ios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.GsonBuilder
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.device.LocalListSimulatorProvider
import com.malinskiy.marathon.ios.device.SimulatorProvider
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceList
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceListDeserializer
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class IOSDeviceProvider(
    private val track: Track,
    private val timer: Timer
) : DeviceProvider, CoroutineScope {

    private val dispatcher = newFixedThreadPoolContext(1, "IOSDeviceProvider")
    override val coroutineContext: CoroutineContext
        get() = dispatcher

    private val logger = MarathonLogging.logger(IOSDeviceProvider::class.java.simpleName)

    private var simulatorProvider: SimulatorProvider? = null

    override val deviceInitializationTimeoutMillis: Long = 300_000
    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is IOSConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration")
        }

        logger.debug("Initializing IOSDeviceProvider")

        val gson = GsonBuilder().registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer())
            .create()

        val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
            .registerModule(KotlinModule())

        simulatorProvider = LocalListSimulatorProvider(coroutineContext, channel, vendorConfiguration, mapper, gson, track, timer)
        simulatorProvider?.start()
    }

    override suspend fun terminate() {
        withContext(coroutineContext) {
            logger.debug { "Terminating IOS device provider" }
            simulatorProvider?.stop()
            channel.close()
        }
        dispatcher.close()
    }

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    override fun subscribe() = channel
}
