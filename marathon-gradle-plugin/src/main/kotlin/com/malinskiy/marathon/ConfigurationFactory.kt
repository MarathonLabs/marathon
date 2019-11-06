package com.malinskiy.marathon

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.DEFAULT_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.android.DEFAULT_AUTO_GRANT_PERMISSION
import com.malinskiy.marathon.android.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.android.defaultInitTimeoutMillis
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.extensions.extractApplication
import com.malinskiy.marathon.extensions.extractTestApplication
import com.malinskiy.marathon.properties.MarathonProperties
import org.gradle.api.Project
import java.io.File

internal fun createConfiguration(
    project: Project,
    marathonExtensionName: String,
    properties: MarathonProperties,
    sdkDirectory: File,
    flavorName: String,
    applicationVariant: BaseVariant,
    testVariant: TestVariant
): Configuration {

    val targetProject = if (properties.isCommonWorkerEnabled) project.rootProject else project
    val extensionConfig = targetProject.extensions.getByName(marathonExtensionName) as? MarathonExtension ?: MarathonExtension(project)
    val name = if (properties.isCommonWorkerEnabled) project.path + ":" + flavorName else extensionConfig.name

    val instrumentationApk = testVariant.extractTestApplication()
    val applicationApk = applicationVariant.extractApplication()

    val output = getOutputDirectory(targetProject, extensionConfig, flavorName)
    val vendorConfiguration = createAndroidConfiguration(extensionConfig, applicationApk, instrumentationApk, sdkDirectory)

    return Configuration(
        name,
        output,
        extensionConfig.analyticsConfiguration?.toAnalyticsConfiguration(),
        extensionConfig.customAnalyticsTracker,
        extensionConfig.poolingStrategy?.toStrategy(),
        extensionConfig.shardingStrategy?.toStrategy(),
        extensionConfig.sortingStrategy?.toStrategy(),
        extensionConfig.batchingStrategy?.toStrategy(),
        extensionConfig.flakinessStrategy?.toStrategy(),
        extensionConfig.retryStrategy?.toStrategy(),
        extensionConfig.filteringConfiguration?.toFilteringConfiguration(),
        extensionConfig.ignoreFailures,
        extensionConfig.isCodeCoverageEnabled,
        extensionConfig.fallbackToScreenshots,
        extensionConfig.strictMode,
        extensionConfig.uncompletedTestRetryQuota,
        extensionConfig.testClassRegexes?.map { it.toRegex() },
        extensionConfig.includeSerialRegexes?.map { it.toRegex() },
        extensionConfig.excludeSerialRegexes?.map { it.toRegex() },
        extensionConfig.testBatchTimeoutMillis,
        extensionConfig.testOutputTimeoutMillis,
        extensionConfig.debug,
        vendorConfiguration,
        extensionConfig.analyticsTracking
    )
}

private fun getOutputDirectory(project: Project, extensionConfig: MarathonExtension, flavorName: String): File {
    val baseOutputDir = extensionConfig.baseOutputDir?.let {
        File(it)
    } ?: project.buildDir.resolve("reports/marathon")

    return baseOutputDir.resolve(flavorName)
}

private fun createAndroidConfiguration(
    extension: MarathonExtension,
    applicationApk: File?,
    instrumentationApk: File,
    sdkDirectory: File
): AndroidConfiguration {
    val autoGrantPermission = extension.autoGrantPermission ?: DEFAULT_AUTO_GRANT_PERMISSION
    val instrumentationArgs = extension.instrumentationArgs
    val applicationPmClear = extension.applicationPmClear ?: DEFAULT_APPLICATION_PM_CLEAR
    val testApplicationPmClear = extension.testApplicationPmClear ?: DEFAULT_APPLICATION_PM_CLEAR
    val adbInitTimeout = extension.adbInitTimeout ?: defaultInitTimeoutMillis
    val installOptions = extension.installOptions ?: DEFAULT_INSTALL_OPTIONS
    val preferableRecorderType = extension.preferableRecorderType

    return AndroidConfiguration(
        sdkDirectory,
        applicationApk,
        instrumentationApk,
        autoGrantPermission,
        instrumentationArgs,
        applicationPmClear,
        testApplicationPmClear,
        adbInitTimeout,
        installOptions,
        preferableRecorderType
    )
}
