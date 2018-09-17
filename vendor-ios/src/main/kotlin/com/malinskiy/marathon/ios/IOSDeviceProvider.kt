package com.malinskiy.marathon.ios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.GsonBuilder
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandExecutor
import com.malinskiy.marathon.ios.device.LocalListSimulatorProvider
import com.malinskiy.marathon.ios.device.SimulatorProvider
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceList
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceListDeserializer
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import java.net.InetAddress

class IOSDeviceProvider : DeviceProvider {
    override fun terminate() {
    }

    override fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is IOSConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }

        val gson = GsonBuilder().registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer())
                .create()

        val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
                .registerModule(KotlinModule())

        val simulatorProvider: SimulatorProvider = LocalListSimulatorProvider(channel,
                vendorConfiguration.remoteUsername,
                vendorConfiguration.remotePublicKey,
                mapper,
                gson)

        simulatorProvider.start()
    }

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    override fun subscribe() = channel

    override fun lockDevice(device: Device): Boolean {
        return false
    }

    override fun unlockDevice(device: Device): Boolean {
        return false
    }
}
