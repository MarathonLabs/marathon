package com.malinskiy.marathon.ios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.GsonBuilder
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.ios.xcrun.simctl.model.SimctlDeviceList
import com.malinskiy.marathon.ios.xcrun.simctl.model.SimctlDeviceListDeserializer
import com.malinskiy.marathon.log.MarathonLogConfigurator
import org.koin.dsl.module

val AppleVendor = module {
    single<DeviceProvider?> {
        val gson = GsonBuilder()
            .registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer())
            .create()
        val objectMapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
            .registerModule(KotlinModule())
        AppleDeviceProvider(get(), get(), gson, objectMapper, get(), get()) 
    }
    single<TestParser?> { AppleTestParser(get()) }
    single<MarathonLogConfigurator> { AppleLogConfigurator(get()) }
    single<TestBundleIdentifier?> { AppleTestBundleIdentifier() }
}
