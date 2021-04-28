package com.malinskiy.marathon

import com.android.build.api.variant.BuiltArtifactsLoader
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.DEFAULT_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.android.DEFAULT_AUTO_GRANT_PERMISSION
import com.malinskiy.marathon.android.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.android.DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.DEFAULT_ALLURE_CONFIGURATION
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.configuration.TimeoutConfiguration
import com.malinskiy.marathon.android.defaultInitTimeoutMillis
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.exceptions.ExceptionsReporter
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import ddmlibModule
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.koin.core.context.stopKoin
import java.io.File
import javax.inject.Inject

private val log = MarathonLogging.logger {}

open class MarathonRunTask @Inject constructor(objects: ObjectFactory) : DefaultTask(), VerificationTask {
    @Input
    val flavorName: Property<String> = objects.property()

    @InputDirectory
    @PathSensitive(PathSensitivity.NAME_ONLY)
    val instrumentationApkDir: DirectoryProperty = objects.directoryProperty()

    @InputDirectory
    @PathSensitive(PathSensitivity.NAME_ONLY)
    val apkFolder: DirectoryProperty = objects.directoryProperty()

    @Internal
    val builtArtifactsLoader: Property<BuiltArtifactsLoader> = objects.property()

    @InputDirectory
    @PathSensitive(PathSensitivity.NAME_ONLY)
    val sdk: DirectoryProperty = objects.directoryProperty()

    @Input
    val name: Property<String> = objects.property()

    @Input
    @Optional
    val vendor: Property<VendorType> = objects.property()

    @Input
    @Optional
    val bugsnag: Property<Boolean> = objects.property()

    @Input
    @Optional
    val analyticsConfiguration: Property<AnalyticsConfig> = objects.property()

    @Input
    @Optional
    val poolingStrategy: Property<PoolingStrategyConfiguration> = objects.property()

    @Input
    @Optional
    val shardingStrategy: Property<ShardingStrategyConfiguration> = objects.property()

    @Input
    @Optional
    val sortingStrategy: Property<SortingStrategyConfiguration> = objects.property()

    @Input
    @Optional
    val batchingStrategy: Property<BatchingStrategyConfiguration> = objects.property()

    @Input
    @Optional
    val flakinessStrategy: Property<FlakinessStrategyConfiguration> = objects.property()

    @Input
    @Optional
    val retryStrategy: Property<RetryStrategyConfiguration> = objects.property()

    @Input
    @Optional
    val filteringConfiguration: Property<FilteringPluginConfiguration> = objects.property()

    @Input
    @Optional
    val baseOutputDir: Property<String> = objects.property()

    @Input
    @Optional
    val ignoreFailures: Property<Boolean> = objects.property()

    @Input
    @Optional
    val codeCoverageEnabled: Property<Boolean> = objects.property()

    @Input
    @Optional
    val fallbackToScreenshots: Property<Boolean> = objects.property()

    @Input
    @Optional
    val strictMode: Property<Boolean> = objects.property()

    @Input
    @Optional
    val uncompletedTestRetryQuota: Property<Int> = objects.property()
    @Input
    @Optional
    val testClassRegexes: Property<Collection<String>> = objects.property()
    @Input
    @Optional
    val includeSerialRegexes: Property<Collection<String>> = objects.property()
    @Input
    @Optional
    val excludeSerialRegexes: Property<Collection<String>> = objects.property()
    @Input
    @Optional
    val testBatchTimeoutMillis: Property<Long> = objects.property()
    @Input
    @Optional
    val testOutputTimeoutMillis: Property<Long> = objects.property()
    @Input
    @Optional
    val debug: Property<Boolean> = objects.property()
    @Input
    @Optional
    val screenRecordingPolicy: Property<ScreenRecordingPolicy> = objects.property()
    @Input
    @Optional
    val applicationPmClear: Property<Boolean> = objects.property()
    @Input
    @Optional
    val testApplicationPmClear: Property<Boolean> = objects.property()
    @Input
    @Optional
    val adbInitTimeout: Property<Int> = objects.property()
    @Input
    @Optional
    val installOptions: Property<String> = objects.property()
    @Input
    @Optional
    val serialStrategy: Property<SerialStrategy> = objects.property()
    @Input
    @Optional
    val screenRecordConfiguration: Property<ScreenRecordConfiguration> = objects.property()
    @Input
    val analyticsTracking: Property<Boolean> = objects.property()
    @Input
    @Optional
    val deviceInitializationTimeoutMillis: Property<Long> = objects.property()
    @Input
    @Optional
    val waitForDevicesTimeoutMillis: Property<Long> = objects.property()
    @Input
    @Optional
    val allureConfiguration: Property<AllureConfiguration> = objects.property()
    @Input
    @Optional
    val timeoutConfiguration: Property<TimeoutConfiguration> = objects.property()
    @Input
    @Optional
    val fileSyncConfiguration: Property<FileSyncConfiguration> = objects.property()

    //Android specific for now
    @Input
    @Optional
    val autoGrantPermission: Property<Boolean> = objects.property()
    @Input
    @Optional
    val instrumentationArgs: MapProperty<String, String> = objects.mapProperty()

    @Internal
    val exceptionsTracker: Property<ExceptionsReporter> = objects.property()

    private var ignoreFailure: Boolean = false

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")

    @TaskAction
    fun runMarathon() {
        val tracker = exceptionsTracker.get()
        val instrumentationApk = instrumentationApkDir.asFileTree.single { it.extension == "apk" }
        val buildArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
        val applicationApk = buildArtifacts?.elements?.first()?.outputFile

        val baseOutputDir = baseOutputDir.orNull?.let { File(it) } ?: File(project.buildDir, "reports/marathon")
        val output = File(baseOutputDir, flavorName.get())

        val vendorConfiguration = createAndroidConfiguration(File(applicationApk), instrumentationApk)

        val cnf = Configuration(
            name.get(),
            output,
            analyticsConfiguration.orNull?.toAnalyticsConfiguration(),
            poolingStrategy.orNull?.toStrategy(),
            shardingStrategy.orNull?.toStrategy(),
            sortingStrategy.orNull?.toStrategy(),
            batchingStrategy.orNull?.toStrategy(),
            flakinessStrategy.orNull?.toStrategy(),
            retryStrategy.orNull?.toStrategy(),
            filteringConfiguration.orNull?.toFilteringConfiguration(),
            ignoreFailures.orNull,
            codeCoverageEnabled.orNull,
            fallbackToScreenshots.orNull,
            strictMode.orNull,
            uncompletedTestRetryQuota.orNull,
            testClassRegexes.orNull?.map { it.toRegex() },
            includeSerialRegexes.orNull?.map { it.toRegex() },
            excludeSerialRegexes.orNull?.map { it.toRegex() },
            testBatchTimeoutMillis.orNull,
            testOutputTimeoutMillis.orNull,
            debug.orNull,
            screenRecordingPolicy.orNull,
            vendorConfiguration,
            analyticsTracking.orNull,
            deviceInitializationTimeoutMillis.orNull
        )

        val androidConfiguration = cnf.vendorConfiguration as? AndroidConfiguration

        log.info { "Run instrumentation tests ${androidConfiguration?.testApplicationOutput} for app ${androidConfiguration?.applicationOutput}" }
        log.debug { "Output: ${cnf.outputDir}" }
        log.debug { "Ignore failures: ${cnf.ignoreFailures}" }

        UsageAnalytics.enable = cnf.analyticsTracking
        UsageAnalytics.USAGE_TRACKER.trackEvent(Event(TrackActionType.RunType, "gradle"))
        try {
            val application = marathonStartKoin(cnf)
            val marathon: Marathon = application.koin.get()

            val success = marathon.run()
            tracker.end()
            val shouldReportFailure = !cnf.ignoreFailures
            if (!success && shouldReportFailure) {
                throw GradleException("Tests failed! See ${cnf.outputDir}/html/index.html")
            }
        } finally {
            stopKoin()
        }
    }

    private fun createAndroidConfiguration(
        applicationApk: File?,
        instrumentationApk: File
    ): AndroidConfiguration {
        val autoGrantPermission = autoGrantPermission.orNull ?: DEFAULT_AUTO_GRANT_PERMISSION
        val instrumentationArgs = instrumentationArgs.orNull ?: emptyMap<String, String>()
        val applicationPmClear = applicationPmClear.orNull ?: DEFAULT_APPLICATION_PM_CLEAR
        val testApplicationPmClear = testApplicationPmClear.orNull ?: DEFAULT_APPLICATION_PM_CLEAR
        val adbInitTimeout = adbInitTimeout.orNull ?: defaultInitTimeoutMillis
        val installOptions = installOptions.orNull ?: DEFAULT_INSTALL_OPTIONS
        val screenRecordConfiguration = screenRecordConfiguration.orNull ?: ScreenRecordConfiguration()
        val serialStrategy = serialStrategy.orNull ?: SerialStrategy.AUTOMATIC
        val waitForDevicesTimeoutMillis = waitForDevicesTimeoutMillis.orNull ?: DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
        val allureConfiguration = allureConfiguration.orNull ?: DEFAULT_ALLURE_CONFIGURATION

        val implementationModules = when (vendor.orNull ?: VendorType.DDMLIB) {
            VendorType.DDMLIB -> listOf(ddmlibModule)
            VendorType.ADAM -> listOf(adamModule)
        }

        return AndroidConfiguration(
            androidSdk = sdk.get().asFile,
            applicationOutput = applicationApk,
            testApplicationOutput = instrumentationApk,
            implementationModules = implementationModules,
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
        )
    }

    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }
}
