package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.cli.args.EnvironmentConfiguration
import com.malinskiy.marathon.cli.args.FileConfiguration
import com.malinskiy.marathon.cli.args.MarathonCliConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.vendor.VendorConfiguration
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

class ConfigFactory {
    fun create(marathonfile: File, androidSdkDir: File?): Configuration {
        logger.info { "Checking ${marathonfile} config" }

        if (!marathonfile.isFile) {
            logger.error { "No config ${marathonfile.absolutePath} present" }
            throw RuntimeException("No config ${marathonfile.absolutePath} present")
        }

        val config = readConfigfile(marathonfile) ?: throw RuntimeException("Invalid config format")

        return Configuration(
                config.name,
                config.outputDir,
                config.applicationOutput,
                config.testApplicationOutput,

                config.analyticsConfiguration,
                config.poolingStrategy,
                config.shardingStrategy,
                config.sortingStrategy,
                config.batchingStrategy,
                config.flakinessStrategy,
                config.retryStrategy,
                config.filteringConfiguration,
                config.ignoreFailures,
                config.isCodeCoverageEnabled,
                config.fallbackToScreenshots,
                config.testClassRegexes,
                config.includeSerialRegexes,
                config.excludeSerialRegexes,
                config.testOutputTimeoutMillis,
                config.debug,
                config.testPackage,
                config.autoGrantPermission,
                vendorConfiguration = AndroidConfiguration(
                        readEnvironment().androidSdkDir ?: androidSdkDir
                        ?: throw RuntimeException("Android SDK not found")
                )
        )
    }

    fun readEnvironment(): EnvironmentConfiguration {
        val androidSdkDir = System.getenv("ANDROID_HOME")
        return EnvironmentConfiguration(
                androidSdkDir = androidSdkDir?.let { File(it) }
        )
    }

    fun readConfigfile(configFile: File): FileConfiguration? {
        val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
        mapper.registerModule(DeserializeModule())
                .registerModule(KotlinModule())
                .registerModule(JavaTimeModule())

        return mapper.readValue(configFile.bufferedReader(), FileConfiguration::class.java)
    }
}