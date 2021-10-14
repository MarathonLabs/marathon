package com.malinskiy.marathon.config.serialization

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.environment.EnvironmentReader
import com.malinskiy.marathon.config.environment.SystemEnvironmentReader
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.time.InstantTimeProviderImpl
import com.malinskiy.marathon.config.serialization.yaml.DerivedDataFileListProvider
import com.malinskiy.marathon.config.serialization.yaml.DeserializeModule
import com.malinskiy.marathon.config.serialization.yaml.FileListProvider
import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.lookup.StringLookupFactory
import java.io.File

class ConfigurationFactory(
    private val marathonfileDir: File,
    private val fileListProvider: FileListProvider = DerivedDataFileListProvider,
    private val environmentReader: EnvironmentReader = SystemEnvironmentReader(),
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)).apply {
        registerModule(DeserializeModule(InstantTimeProviderImpl(), environmentReader, marathonfileDir, fileListProvider))
        registerModule(KotlinModule())
        registerModule(JavaTimeModule())
    },
    private val environmentVariableSubstitutor: StringSubstitutor = StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup()),
) {
    fun parse(marathonfile: File): Configuration {
        val configWithEnvironmentVariablesReplaced = environmentVariableSubstitutor.replace(marathonfile.readText())
        try {
            return mapper.readValue(configWithEnvironmentVariablesReplaced, Configuration::class.java)
        } catch (e: JsonProcessingException) {
            throw ConfigurationException("Error parsing config file ${marathonfile.absolutePath}", e)
        }
    }
}
