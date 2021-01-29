package com.malinskiy.marathon

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.DEFAULT_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.android.DEFAULT_AUTO_GRANT_PERMISSION
import com.malinskiy.marathon.android.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.android.DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.configuration.DEFAULT_ALLURE_CONFIGURATION
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.defaultInitTimeoutMillis
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.exceptions.ExceptionsReporter
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.extensions.extractApplication
import com.malinskiy.marathon.extensions.extractTestApplication
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.koin.core.context.stopKoin
import java.io.File

private val log = MarathonLogging.logger {}

open class MarathonRunTask : DefaultTask(), VerificationTask {
    lateinit var flavorName: String
    lateinit var applicationVariant: BaseVariant
    lateinit var testVariant: TestVariant
    lateinit var extensionConfig: MarathonExtension
    lateinit var sdk: File
    lateinit var cnf: Configuration
    lateinit var exceptionsTracker: ExceptionsReporter
    var ignoreFailure: Boolean = false

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")

    @TaskAction
    fun runMarathon() {
        val instrumentationApk = testVariant.extractTestApplication()
        val applicationApk = applicationVariant.extractApplication()

        val baseOutputDir =
            if (extensionConfig.baseOutputDir != null) File(extensionConfig.baseOutputDir) else File(project.buildDir, "reports/marathon")
        val output = File(baseOutputDir, flavorName)

        val vendorConfiguration = createAndroidConfiguration(extensionConfig, applicationApk, instrumentationApk)

        cnf = Configuration(
            extensionConfig.name,
            output,
            extensionConfig.analyticsConfiguration?.toAnalyticsConfiguration(),
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
            extensionConfig.screenRecordingPolicy,
            vendorConfiguration,
            extensionConfig.analyticsTracking,
            extensionConfig.deviceInitializationTimeoutMillis
        )

        val androidConfiguration = cnf.vendorConfiguration as? AndroidConfiguration

        log.info { "Run instrumentation tests ${androidConfiguration?.testApplicationApk} for app ${androidConfiguration?.applicationApk}" }
        log.debug { "Output: ${cnf.outputDir}" }
        log.debug { "Ignore failures: ${cnf.ignoreFailures}" }

        UsageAnalytics.enable = cnf.analyticsTracking
        UsageAnalytics.USAGE_TRACKER.trackEvent(Event(TrackActionType.RunType, "gradle"))
        try {
            val application = marathonStartKoin(cnf)
            val marathon: Marathon = application.koin.get()

            val success = marathon.run()
            exceptionsTracker.end()
            val shouldReportFailure = !cnf.ignoreFailures
            if (!success && shouldReportFailure) {
                throw GradleException("Tests failed! See ${cnf.outputDir}/html/index.html")
            }
        } finally {
            stopKoin()
        }
    }

    private fun createAndroidConfiguration(
        extension: MarathonExtension,
        applicationApk: File?,
        instrumentationApk: File
    ): AndroidConfiguration {
        val autoGrantPermission = extension.autoGrantPermission ?: DEFAULT_AUTO_GRANT_PERMISSION
        val instrumentationArgs = extension.instrumentationArgs
        val applicationPmClear = extension.applicationPmClear ?: DEFAULT_APPLICATION_PM_CLEAR
        val testApplicationPmClear = extension.testApplicationPmClear ?: DEFAULT_APPLICATION_PM_CLEAR
        val adbInitTimeout = extension.adbInitTimeout ?: defaultInitTimeoutMillis
        val installOptions = extension.installOptions ?: DEFAULT_INSTALL_OPTIONS
        val screenRecordConfiguration = extension.screenRecordConfiguration ?: ScreenRecordConfiguration()
        val serialStrategy = extension.serialStrategy ?: SerialStrategy.AUTOMATIC
        val waitForDevicesTimeoutMillis = extension.waitForDevicesTimeoutMillis ?: DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
        val allureConfiguration = extension.allureConfiguration ?: DEFAULT_ALLURE_CONFIGURATION

        return AndroidConfiguration(
            androidSdk = sdk,
            applicationApk = applicationApk,
            testApplicationApk = instrumentationApk,
            vendorType = extension.vendor ?: VendorType.DDMLIB,
            autoGrantPermission = autoGrantPermission,
            instrumentationArgs = instrumentationArgs,
            applicationPmClear = applicationPmClear,
            testApplicationPmClear = testApplicationPmClear,
            adbInitTimeoutMillis = adbInitTimeout,
            installOptions = installOptions,
            screenRecordConfiguration = screenRecordConfiguration,
            serialStrategy = serialStrategy,
            waitForDevicesTimeoutMillis = waitForDevicesTimeoutMillis,
            allureConfiguration = allureConfiguration
        )
    }

    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }
}
