package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.malinskiy.marathon.android.AndroidVendor
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.cli.args.FileAndroidConfiguration
import com.malinskiy.marathon.cli.args.FileConfiguration
import com.malinskiy.marathon.cli.args.FileIOSConfiguration
import com.malinskiy.marathon.cli.args.environment.EnvironmentReader
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.ios.IOSVendor
import com.malinskiy.marathon.log.MarathonLogging
import ddmlibModule
import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.lookup.StringLookupFactory
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

private val logger = MarathonLogging.logger {}

class ConfigFactory(private val mapper: ObjectMapper) {
    private val environmentVariableSubstitutor = StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup())

    fun create(marathonfile: File, environmentReader: EnvironmentReader): Pair<Configuration, List<Module>> {
        logger.info { "Checking $marathonfile config" }

        if (!marathonfile.isFile) {
            logger.error { "No config ${marathonfile.absolutePath} present" }
            throw ConfigurationException("No config ${marathonfile.absolutePath} present")
        }

        val config = readConfigFile(marathonfile) ?: throw ConfigurationException("Invalid config format")

        val fileVendorConfiguration = config.vendorConfiguration
        val (vendorConfiguration, modules) = when (fileVendorConfiguration) {
            is FileIOSConfiguration -> {
                val iosConfiguration = fileVendorConfiguration.toIOSConfiguration(
                    marathonfile.canonicalFile.parentFile
                )
                Pair(iosConfiguration, IOSVendor + module { single { iosConfiguration } })
            }
            is FileAndroidConfiguration -> {
                val androidConfiguration = fileVendorConfiguration.toAndroidConfiguration(environmentReader.read().androidSdk)
                val implementationModules = when (fileVendorConfiguration.vendor) {
                    VendorType.ADAM -> listOf(adamModule)
                    VendorType.DDMLIB -> listOf(ddmlibModule)
                }
                Pair(androidConfiguration, AndroidVendor + implementationModules + module { single { androidConfiguration } })
            }
            else -> throw ConfigurationException("No vendor config present in ${marathonfile.absolutePath}")
        }

        val configuration = Configuration(
            name = config.name,
            outputDir = config.outputDir,

            analyticsConfiguration = config.analyticsConfiguration,
            poolingStrategy = config.poolingStrategy,
            shardingStrategy = config.shardingStrategy,
            sortingStrategy = config.sortingStrategy,
            batchingStrategy = config.batchingStrategy,
            flakinessStrategy = config.flakinessStrategy,
            retryStrategy = config.retryStrategy,
            filteringConfiguration = config.filteringConfiguration,
            ignoreFailures = config.ignoreFailures,
            isCodeCoverageEnabled = config.isCodeCoverageEnabled,
            fallbackToScreenshots = config.fallbackToScreenshots,
            strictMode = config.strictMode,
            uncompletedTestRetryQuota = config.uncompletedTestRetryQuota,
            testClassRegexes = config.testClassRegexes,
            includeSerialRegexes = config.includeSerialRegexes,
            excludeSerialRegexes = config.excludeSerialRegexes,
            testBatchTimeoutMillis = config.testBatchTimeoutMillis,
            testOutputTimeoutMillis = config.testOutputTimeoutMillis,
            debug = config.debug,
            screenRecordingPolicy = config.screenRecordingPolicy,
            vendorConfiguration = vendorConfiguration,
            analyticsTracking = config.analyticsTracking,
            deviceInitializationTimeoutMillis = config.deviceInitializationTimeoutMillis
        )
        return Pair(configuration, modules)
    }

    private fun readConfigFile(configFile: File): FileConfiguration? {
        val configWithEnvironmentVariablesReplaced = environmentVariableSubstitutor.replace(configFile.readText())
        try {
            return mapper.readValue(configWithEnvironmentVariablesReplaced, FileConfiguration::class.java)
        } catch (e: JsonProcessingException) {
            logger.error(e) { "Error parsing config file ${configFile.absolutePath}" }
            throw ConfigurationException(e)
        }
    }
}
