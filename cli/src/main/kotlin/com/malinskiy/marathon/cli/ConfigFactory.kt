package com.malinskiy.marathon.cli

import com.malinskiy.marathon.cli.args.FileAndroidConfiguration
import com.malinskiy.marathon.cli.args.FileConfiguration
import com.malinskiy.marathon.cli.args.FileIOSConfiguration
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.vendor.VendorConfiguration
import com.sksamuel.hoplite.ConfigLoader
import java.io.File

private val logger = MarathonLogging.logger {}

class ConfigFactory(private val loader: ConfigLoader) {


    fun create(marathonfile: File): Configuration {
        logger.info { "Checking $marathonfile config" }

        val config = loader.loadConfigOrThrow<FileConfiguration>(marathonfile)

        val fileVendorConfiguration = config.vendorConfiguration
        val vendorConfiguration = when (fileVendorConfiguration) {
            is FileIOSConfiguration -> fileVendorConfiguration.toIOSConfiguration(
                marathonfile.canonicalFile.parentFile
            )
            is FileAndroidConfiguration -> {
                fileVendorConfiguration.toAndroidConfiguration()
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
            screenRecordingPolicy = config.screenRecordingPolicy,
            vendorConfiguration = vendorConfiguration as VendorConfiguration,
            analyticsTracking = config.analyticsTracking,
            deviceInitializationTimeoutMillis = config.deviceInitializationTimeoutMillis
        )
    }
}
