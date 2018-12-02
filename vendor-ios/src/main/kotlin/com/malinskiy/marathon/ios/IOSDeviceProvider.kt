package com.malinskiy.marathon.ios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.GsonBuilder
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.device.LocalListSimulatorProvider
import com.malinskiy.marathon.ios.device.SimulatorProvider
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceList
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceListDeserializer
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.channels.Channel

class IOSDeviceProvider : DeviceProvider {

    private val logger = MarathonLogging.logger(IOSDeviceProvider::class.java.simpleName)

    private lateinit var simulatorProvider: SimulatorProvider

    override fun terminate() {
        logger.debug { "Terminating IOS device provider" }
        simulatorProvider?.stop()
        channel.close()
    }

    override fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is IOSConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }

        val gson = GsonBuilder().registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer())
                .create()

        val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
                .registerModule(KotlinModule())

        simulatorProvider = LocalListSimulatorProvider(channel,
                vendorConfiguration,
                mapper,
                gson)

        simulatorProvider.start()
    }

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    override fun subscribe() = channel

}
