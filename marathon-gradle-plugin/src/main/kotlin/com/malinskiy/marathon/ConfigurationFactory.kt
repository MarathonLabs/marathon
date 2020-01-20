package com.malinskiy.marathon

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.android.AndroidComponentInfo
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.DEFAULT_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.android.DEFAULT_AUTO_GRANT_PERMISSION
import com.malinskiy.marathon.android.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.android.DEFAULT_SERIAL_STRATEGY
import com.malinskiy.marathon.android.defaultInitTimeoutMillis
import com.malinskiy.marathon.android.serial.SerialStrategy
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.extensions.extractApplication
import com.malinskiy.marathon.extensions.extractTestApplication
import org.gradle.api.Project
import java.io.File

internal fun createCommonConfiguration(
    project: Project,
    marathonExtensionName: String,
    sdkDirectory: File
): Configuration {
    val targetProject = project.rootProject
    val extensionConfig = targetProject.extensions.getByName(marathonExtensionName) as? MarathonExtension ?: MarathonExtension(project)

    val output = getOutputDirectory(targetProject, extensionConfig, null)
    val fakeApk = File(".")

    return createConfiguration(
        extensionConfig = extensionConfig,
        applicationApk = null,
        instrumentationApk = fakeApk,
        sdkDirectory = sdkDirectory,
        name = extensionConfig.name,
        output = output
    )
}

internal fun createComponentInfo(
    project: Project,
    flavorName: String,
    applicationVariant: BaseVariant,
    testVariant: TestVariant
): AndroidComponentInfo {
    val name = project.path + ":" + flavorName

    val instrumentationApk = testVariant.extractTestApplication()
    val applicationApk = applicationVariant.extractApplication()

    return AndroidComponentInfo(name = name, applicationOutput = applicationApk, testApplicationOutput = instrumentationApk)
}

internal fun createConfiguration(
    project: Project,
    marathonExtensionName: String,
    sdkDirectory: File,
    flavorName: String,
    applicationVariant: BaseVariant,
    testVariant: TestVariant
): Configuration {
    val extensionConfig = project.extensions.getByName(marathonExtensionName) as? MarathonExtension ?: MarathonExtension(project)
    val instrumentationApk = testVariant.extractTestApplication()
    val applicationApk = applicationVariant.extractApplication()

    return createConfiguration(
        extensionConfig = extensionConfig,
        applicationApk = applicationApk,
        instrumentationApk = instrumentationApk,
        sdkDirectory = sdkDirectory,
        name = extensionConfig.name,
        output = getOutputDirectory(project, extensionConfig, flavorName)
    )
}

private fun createConfiguration(
    extensionConfig: MarathonExtension,
    applicationApk: File?,
    instrumentationApk: File,
    sdkDirectory: File,
    name: String,
    output: File
): Configuration = Configuration(
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
    extensionConfig.strictRunFilterConfiguration?.toStrictRunFilterConfiguration(),
    extensionConfig.cache?.toCacheConfiguration(),
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
    createAndroidConfiguration(extensionConfig, applicationApk, instrumentationApk, sdkDirectory),
    extensionConfig.analyticsTracking
)

private fun getOutputDirectory(project: Project, extensionConfig: MarathonExtension, flavorName: String?): File {
    val baseOutputDir = extensionConfig.baseOutputDir?.let {
        File(it)
    } ?: project.buildDir.resolve("reports/marathon")

    return flavorName?.let { baseOutputDir.resolve(it) } ?: baseOutputDir
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
    val serialStrategy = extension.serialStrategy
        ?.let {
            when (it) {
                SerialStrategyConfiguration.AUTOMATIC -> SerialStrategy.AUTOMATIC
                SerialStrategyConfiguration.MARATHON_PROPERTY -> SerialStrategy.MARATHON_PROPERTY
                SerialStrategyConfiguration.BOOT_PROPERTY -> SerialStrategy.BOOT_PROPERTY
                SerialStrategyConfiguration.HOSTNAME -> SerialStrategy.HOSTNAME
                SerialStrategyConfiguration.DDMS -> SerialStrategy.DDMS
            }
        }
        ?: DEFAULT_SERIAL_STRATEGY

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
        preferableRecorderType,
        serialStrategy
    )
}
