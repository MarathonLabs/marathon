package com.malinskiy.marathon.gradle.service

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.serialization.MutableConfigurationFactory
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class JsonService : BuildService<BuildServiceParameters.None> {
    private val factory = MutableConfigurationFactory()

    fun serialize(configuration : Configuration.Builder) = factory.serialize(configuration)
    fun serialize(configuration : VendorConfiguration.AndroidConfigurationBuilder) = factory.serialize(configuration)
    fun parse(value: String) = factory.parse(value)
    fun parseVendor(value: String) = factory.parseVendorAndroid(value)
}
