package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.malinskiy.marathon.cli.args.FileAndroidConfiguration
import com.malinskiy.marathon.cli.args.FileConfiguration
import com.malinskiy.marathon.cli.args.FileIOSConfiguration
import com.malinskiy.marathon.cli.args.environment.EnvironmentReader
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

private val logger = MarathonLogging.logger {}

class ConfigFactory(private val mapper: ObjectMapper) {
    fun create(marathonfile: File, environmentReader: EnvironmentReader): Configuration {
        logger.info { "Checking $marathonfile config" }

        if (!marathonfile.isFile) {
            logger.error { "No config ${marathonfile.absolutePath} present" }
            throw ConfigurationException("No config ${marathonfile.absolutePath} present")
        }

        val config = readConfigFile(marathonfile) ?: throw ConfigurationException("Invalid config format")

        val fileVendorConfiguration = config.vendorConfiguration
        val vendorConfiguration = when (fileVendorConfiguration) {
            is FileIOSConfiguration -> fileVendorConfiguration.toIOSConfiguration(
                marathonfile.canonicalFile.parentFile
            )
            is FileAndroidConfiguration -> {
                fileVendorConfiguration.toAndroidConfiguration(environmentReader.read().androidSdk)
            }
            else -> throw ConfigurationException("No vendor config present in ${marathonfile.absolutePath}")
        }

        return Configuration(
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
            vendorConfiguration = vendorConfiguration as VendorConfiguration,
            analyticsTracking = config.analyticsTracking
        )
    }

    private fun readConfigFile(configFile: File): FileConfiguration? {
        try {
            return mapper.readValue(configFile.bufferedReader(), FileConfiguration::class.java)
        } catch (e: MismatchedInputException) {
            logger.error { "Invalid config file ${configFile.absolutePath}. Error parsing ${e.targetType.canonicalName}" }
            throw ConfigurationException(e)
        }
    }
}
