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
import java.io.File

private val logger = MarathonLogging.logger {}

class ConfigFactory(val mapper: ObjectMapper) {
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
                config.name,
                config.outputDir,

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
                config.testSuiteNameMatchesClassName,
                config.testClassRegexes,
                config.includeSerialRegexes,
                config.excludeSerialRegexes,
                config.testBatchTimeoutMillis,
                config.testOutputTimeoutMillis,
                config.debug,
                vendorConfiguration,
                config.analyticsTracking
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
