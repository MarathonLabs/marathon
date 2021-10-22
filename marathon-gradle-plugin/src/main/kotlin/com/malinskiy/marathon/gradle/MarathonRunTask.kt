package com.malinskiy.marathon.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.serialization.ConfigurationFactory
import com.malinskiy.marathon.config.vendor.DEFAULT_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.config.vendor.DEFAULT_AUTO_GRANT_PERMISSION
import com.malinskiy.marathon.config.vendor.DEFAULT_INIT_TIMEOUT_MILLIS
import com.malinskiy.marathon.config.vendor.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.config.vendor.DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.gradle.extensions.extractApplication
import com.malinskiy.marathon.gradle.extensions.extractTestApplication
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.VerificationTask
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

open class MarathonRunTask @Inject constructor(objects: ObjectFactory) : AbstractExecTask<MarathonRunTask>(MarathonRunTask::class.java),
    VerificationTask {
    @Input
    val flavorName: Property<String> = objects.property()

    @Internal
    val applicationVariant: Property<BaseVariant> = objects.property()

    @Internal
    val testVariant: Property<TestVariant> = objects.property()

    @InputDirectory
    @PathSensitive(PathSensitivity.NAME_ONLY)
    val sdk: DirectoryProperty = objects.directoryProperty()

    @Internal
    val marathonExtension: Property<MarathonExtension> = objects.property()

    private var ignoreFailure: Boolean = false

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")

    override fun exec() {
        val extensionConfig = marathonExtension.get()
        val instrumentationApk = testVariant.get().extractTestApplication()
        val applicationApk = applicationVariant.get().extractApplication()

        val baseOutputDir = extensionConfig.baseOutputDir?.let { File(it) } ?: File(project.buildDir, "reports/marathon")
        val output = File(baseOutputDir, flavorName.get())

        val vendorConfiguration = createAndroid(extensionConfig, applicationApk, instrumentationApk)

        val cnf = Configuration.Builder(
            name = extensionConfig.name,
            outputDir = output,
            vendorConfiguration = vendorConfiguration
        ).apply {
            extensionConfig.analyticsConfiguration?.toAnalyticsConfiguration()?.let { analyticsConfiguration = it }
            extensionConfig.poolingStrategy?.toStrategy()?.let { poolingStrategy = it }
            extensionConfig.shardingStrategy?.toStrategy()?.let { shardingStrategy = it }
            extensionConfig.sortingStrategy?.toStrategy()?.let { sortingStrategy = it }
            extensionConfig.batchingStrategy?.toStrategy()?.let { batchingStrategy = it }
            extensionConfig.flakinessStrategy?.toStrategy()?.let { flakinessStrategy = it }
            extensionConfig.retryStrategy?.toStrategy()?.let { retryStrategy = it }
            extensionConfig.filteringConfiguration?.toFilteringConfiguration()?.let { filteringConfiguration = it }
            extensionConfig.ignoreFailures?.let { ignoreFailures = it }
            extensionConfig.isCodeCoverageEnabled?.let { isCodeCoverageEnabled = it }
            extensionConfig.fallbackToScreenshots?.let { fallbackToScreenshots = it }
            extensionConfig.strictMode?.let { strictMode = it }
            extensionConfig.uncompletedTestRetryQuota?.let { uncompletedTestRetryQuota = it }
            extensionConfig.testClassRegexes?.map { it.toRegex() }?.let { testClassRegexes = it }
            extensionConfig.includeSerialRegexes?.map { it.toRegex() }?.let { includeSerialRegexes = it }
            extensionConfig.excludeSerialRegexes?.map { it.toRegex() }?.let { excludeSerialRegexes = it }
            extensionConfig.testBatchTimeoutMillis?.let { testBatchTimeoutMillis = it }
            extensionConfig.testOutputTimeoutMillis?.let { testOutputTimeoutMillis = it }
            extensionConfig.debug?.let { debug = it }
            extensionConfig.screenRecordingPolicy?.let { screenRecordingPolicy = it }
            extensionConfig.analyticsTracking?.let { analyticsTracking = it }
            extensionConfig.deviceInitializationTimeoutMillis?.let { deviceInitializationTimeoutMillis = deviceInitializationTimeoutMillis }
        }.build()

        //Write a Marathonfile
        val marathonfile = File(temporaryDir, "Marathonfile")
        val configurationFactory = ConfigurationFactory(marathonfileDir = baseOutputDir)
        val yaml = configurationFactory.serialize(cnf)
        marathonfile.writeText(yaml)

        setArgs(listOf("-m", marathonfile.canonicalPath))

        super.exec()
    }

    private fun createAndroid(
        extension: MarathonExtension,
        applicationApk: File?,
        instrumentationApk: File
    ): VendorConfiguration.AndroidConfiguration {
        val autoGrantPermission = extension.autoGrantPermission ?: DEFAULT_AUTO_GRANT_PERMISSION
        val instrumentationArgs = extension.instrumentationArgs
        val applicationPmClear = extension.applicationPmClear ?: DEFAULT_APPLICATION_PM_CLEAR
        val testApplicationPmClear = extension.testApplicationPmClear ?: DEFAULT_APPLICATION_PM_CLEAR
        val adbInitTimeout = extension.adbInitTimeout ?: DEFAULT_INIT_TIMEOUT_MILLIS
        val installOptions = extension.installOptions ?: DEFAULT_INSTALL_OPTIONS
        val screenRecordConfiguration = extension.screenRecordConfiguration ?: ScreenRecordConfiguration()
        val serialStrategy = extension.serialStrategy ?: SerialStrategy.AUTOMATIC
        val waitForDevicesTimeoutMillis = extension.waitForDevicesTimeoutMillis ?: DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
        val allureConfiguration = extension.allureConfiguration ?: AllureConfiguration()

        return VendorConfiguration.AndroidConfiguration(
            vendor = extension.vendor ?: VendorConfiguration.AndroidConfiguration.VendorType.ADAM,
            androidSdk = sdk.get().asFile,
            applicationOutput = applicationApk,
            testApplicationOutput = instrumentationApk,
            autoGrantPermission = autoGrantPermission,
            instrumentationArgs = instrumentationArgs,
            applicationPmClear = applicationPmClear,
            testApplicationPmClear = testApplicationPmClear,
            adbInitTimeoutMillis = adbInitTimeout,
            installOptions = installOptions,
            screenRecordConfiguration = screenRecordConfiguration,
            serialStrategy = serialStrategy,
            waitForDevicesTimeoutMillis = waitForDevicesTimeoutMillis,
            allureConfiguration = allureConfiguration,
            fileSyncConfiguration = extension.fileSyncConfiguration ?: FileSyncConfiguration(),
        )
    }

    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }
}
