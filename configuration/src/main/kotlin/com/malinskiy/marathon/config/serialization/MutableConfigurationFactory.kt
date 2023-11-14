package com.malinskiy.marathon.config.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.serialization.time.InstantTimeProviderImpl
import com.malinskiy.marathon.config.serialization.yaml.SerializeModule
import com.malinskiy.marathon.config.vendor.VendorConfiguration

class MutableConfigurationFactory {
    private val mapper: ObjectMapper = ObjectMapper(
        YAMLFactory()
            .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
    ).apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, true)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        registerModule(JavaTimeModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    fun parse(value: String) = mapper.readValue(value, Configuration.Builder::class.java)
    fun parseVendorAndroid(value: String) = mapper.readValue(value, VendorConfiguration.AndroidConfigurationBuilder::class.java)
    fun serialize(configuration: Configuration.Builder) = mapper.writeValueAsString(configuration)
    fun serialize(configuration : VendorConfiguration.AndroidConfigurationBuilder) = mapper.writeValueAsString(configuration)
}
