package com.malinskiy.marathon.lite


import com.android.build.api.variant.BuiltArtifactsLoader
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.DEFAULT_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.android.DEFAULT_AUTO_GRANT_PERMISSION
import com.malinskiy.marathon.android.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.android.DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.android.configuration.DEFAULT_ALLURE_CONFIGURATION
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.defaultInitTimeoutMillis
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.exceptions.ExceptionsReporter
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import ddmlibModule
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.gradle.kotlin.dsl.property
import org.koin.core.context.stopKoin
import java.io.File
import javax.inject.Inject

private val log = MarathonLogging.logger {}

open class MarathonRunTask @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask(), VerificationTask {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    internal val instrumentationApkDir: DirectoryProperty = objects.directoryProperty()

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    internal val apkFolder: DirectoryProperty = objects.directoryProperty()

    @get:Internal
    internal val builtArtifactsLoader: Property<BuiltArtifactsLoader> = objects.property()

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    internal val sdk: DirectoryProperty = objects.directoryProperty()

    @get:Input
    internal val flavorName: Property<String> = objects.property()

    @get:Internal
    internal val exceptionsTracker: Property<ExceptionsReporter> = objects.property()

    private var ignoreFailure: Boolean = false

    //TODO: Make every field of marathon extension @Input
    @get:Internal
    val extension : Property<MarathonExtension> = objects.property()

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")

    @TaskAction
    fun runMarathon() {
        val instrumentationApk = instrumentationApkDir.asFileTree.single { it.extension == "apk" }
        val buildArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
        val applicationApk = buildArtifacts?.elements?.first()?.outputFile
        val extensionConfig = extension.get()
        val baseOutputDir = File(extensionConfig.baseOutputDir.getOrElse(project.buildDir.toString()), "reports/marathon")
        val output = File(baseOutputDir, flavorName.get())

        val vendorConfiguration = createAndroidConfiguration(extensionConfig, applicationApk?.let { File(it) }, instrumentationApk)
//        val vendorConfiguration = createAndroidConfiguration(extensionConfig, File(apkFolder.get()), instrumentationApk)

        val cnf = Configuration(
            extensionConfig.name.getOrElse("marathon"),
            output,
            extensionConfig.analyticsConfiguration.orNull?.toAnalyticsConfiguration(),
            extensionConfig.poolingStrategy.orNull?.toStrategy(),
            extensionConfig.shardingStrategy.orNull?.toStrategy(),
            extensionConfig.sortingStrategy.orNull?.toStrategy(),
            extensionConfig.batchingStrategy.orNull?.toStrategy(),
            extensionConfig.flakinessStrategy.orNull?.toStrategy(),
            extensionConfig.retryStrategy.orNull?.toStrategy(),
            extensionConfig.filteringConfiguration.orNull?.toFilteringConfiguration(),
            extensionConfig.ignoreFailures.orNull,
            extensionConfig.isCodeCoverageEnabled.orNull,
            extensionConfig.fallbackToScreenshots.orNull,
            extensionConfig.strictMode.orNull,
            extensionConfig.uncompletedTestRetryQuota.orNull,
            extensionConfig.testClassRegexes.orNull?.map { it.toRegex() },
            extensionConfig.includeSerialRegexes.orNull?.map { it.toRegex() },
            extensionConfig.excludeSerialRegexes.orNull?.map { it.toRegex() },
            extensionConfig.testBatchTimeoutMillis.orNull,
            extensionConfig.testOutputTimeoutMillis.orNull,
            extensionConfig.debug.orNull,
            extensionConfig.screenRecordingPolicy.orNull,
            vendorConfiguration,
            extensionConfig.analyticsTracking.orNull,
            extensionConfig.deviceInitializationTimeoutMillis.orNull
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
            exceptionsTracker.get().end()
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
        val autoGrantPermission = extension.autoGrantPermission.getOrElse(DEFAULT_AUTO_GRANT_PERMISSION)
        val instrumentationArgs = extension.instrumentationArgs.getOrElse(mutableMapOf())
        val applicationPmClear = extension.applicationPmClear.getOrElse(DEFAULT_APPLICATION_PM_CLEAR)
        val testApplicationPmClear = extension.testApplicationPmClear.getOrElse(DEFAULT_APPLICATION_PM_CLEAR)
        val adbInitTimeout = extension.adbInitTimeout.getOrElse(defaultInitTimeoutMillis)
        val installOptions = extension.installOptions.getOrElse(DEFAULT_INSTALL_OPTIONS)
        val screenRecordConfiguration = extension.screenRecordConfiguration.getOrElse(ScreenRecordConfiguration())
        val serialStrategy = extension.serialStrategy.getOrElse(SerialStrategy.AUTOMATIC)
        val waitForDevicesTimeoutMillis = extension.waitForDevicesTimeoutMillis.getOrElse(DEFAULT_WAIT_FOR_DEVICES_TIMEOUT)
        val allureConfiguration = extension.allureConfiguration.getOrElse(DEFAULT_ALLURE_CONFIGURATION)

        val implementationModules = when (extension.vendor.getOrElse(VendorType.DDMLIB)) {
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
            allureConfiguration = allureConfiguration
        )
    }

    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }
}
