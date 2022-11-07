package com.malinskiy.marathon.config.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.environment.EnvironmentReader
import com.malinskiy.marathon.config.environment.SystemEnvironmentReader
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.time.InstantTimeProviderImpl
import com.malinskiy.marathon.config.serialization.yaml.DerivedDataFileListProvider
import com.malinskiy.marathon.config.serialization.yaml.FileListProvider
import com.malinskiy.marathon.config.serialization.yaml.SerializeModule
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.lookup.StringLookupFactory
import java.io.File

class ConfigurationFactory(
    private val marathonfileDir: File,
    private val fileListProvider: FileListProvider = DerivedDataFileListProvider,
    private val environmentReader: EnvironmentReader = SystemEnvironmentReader(),
    private val mapper: ObjectMapper = ObjectMapper(
        YAMLFactory()
            .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
    ).apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        registerModule(SerializeModule(InstantTimeProviderImpl(), environmentReader, marathonfileDir, fileListProvider))
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
    },
    private val environmentVariableSubstitutor: StringSubstitutor = StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup()),
) {
    fun parse(marathonfile: File): Configuration {
        val configWithEnvironmentVariablesReplaced = environmentVariableSubstitutor.replace(marathonfile.readText())
        try {
            val configuration = mapper.readValue(configWithEnvironmentVariablesReplaced, Configuration::class.java)
            val vendorConfiguration = when (configuration.vendorConfiguration) {
                is VendorConfiguration.AndroidConfiguration -> {
                    if (configuration.vendorConfiguration.androidSdk == null) {
                        val androidSdk =
                            environmentReader.read().androidSdk ?: throw ConfigurationException("No android SDK path specified")
                        configuration.vendorConfiguration.copy(
                            androidSdk = androidSdk
                        )
                    } else {
                        configuration.vendorConfiguration
                    }
                }

                is VendorConfiguration.IOSConfiguration -> {
                    // Any relative path specified in Marathonfile should be resolved against the directory Marathonfile is in
                    val resolvedDerivedDataDir = marathonfileDir.resolve(configuration.vendorConfiguration.derivedDataDir)
                    val finalXCTestRunPath = configuration.vendorConfiguration.xctestrunPath?.resolveAgainst(marathonfileDir)
                        ?: fileListProvider
                            .fileList(resolvedDerivedDataDir)
                            .firstOrNull { it.extension == "xctestrun" }
                        ?: throw ConfigurationException("Unable to find an xctestrun file in derived data folder")
                    val optionalSourceRoot = configuration.vendorConfiguration.sourceRoot.resolveAgainst(marathonfileDir)
                    val optionalDevices = configuration.vendorConfiguration.devicesFile?.resolveAgainst(marathonfileDir)
                        ?: marathonfileDir.resolve("Marathondevices")
                    val optionalKnownHostsPath = configuration.vendorConfiguration.knownHostsPath?.resolveAgainst(marathonfileDir)

                    configuration.vendorConfiguration.copy(
                        derivedDataDir = resolvedDerivedDataDir,
                        xctestrunPath = finalXCTestRunPath,
                        sourceRoot = optionalSourceRoot,
                        devicesFile = optionalDevices,
                        knownHostsPath = optionalKnownHostsPath,
                    )
                }

                VendorConfiguration.StubVendorConfiguration -> configuration.vendorConfiguration
                is VendorConfiguration.EmptyVendorConfiguration -> throw ConfigurationException("No vendor configuration specified")
            }

            return configuration.copy(vendorConfiguration = vendorConfiguration)
        } catch (e: JsonProcessingException) {
            throw ConfigurationException("Error parsing config file ${marathonfile.absolutePath}", e)
        }
    }

    fun serialize(configuration: Configuration): String {
        return mapper.writeValueAsString(configuration)
    }
}

// inverted [resolve] call allows to avoid too many if expressions
private fun File.resolveAgainst(file: File): File = file.resolve(this).canonicalFile
