package com.malinskiy.marathon.gradle

import com.android.build.api.variant.BuiltArtifacts
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.serialization.ConfigurationFactory
import com.malinskiy.marathon.config.vendor.DEFAULT_APPLICATION_PM_CLEAR
import com.malinskiy.marathon.config.vendor.DEFAULT_AUTO_GRANT_PERMISSION
import com.malinskiy.marathon.config.vendor.DEFAULT_DISABLE_WINDOW_ANIMATION
import com.malinskiy.marathon.config.vendor.DEFAULT_INIT_TIMEOUT_MILLIS
import com.malinskiy.marathon.config.vendor.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.config.vendor.DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AdbEndpoint
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.AndroidTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.config.vendor.android.TestAccessConfiguration
import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration
import com.malinskiy.marathon.config.vendor.android.TimeoutConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

open class GenerateMarathonfileTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    init {
        group = GROUP
    }

    @Input
    val flavorName: Property<String> = objects.property()

    @Internal
    val applicationBundles: ListProperty<GradleAndroidTestBundle> = objects.listProperty()

    @InputDirectory
    @PathSensitive(PathSensitivity.NAME_ONLY)
    val sdk: DirectoryProperty = objects.directoryProperty()
    
    @Input
    val configurationBuilder: Property<Configuration.Builder> = objects.property()

    @Input
    val vendorConfigurationBuilder: Property<VendorConfiguration.AndroidConfigurationBuilder> = objects.property()

    @OutputFile
    val marathonfile: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun write() {
        val androidConfiguration = vendorConfigurationBuilder.get().build()
        val cnf = configurationBuilder.get()
//        val cnf = Configuration.Builder(
//            name = extensionConfig.name,
//            outputDir = output,
//            vendorConfiguration = vendorConfiguration
//        ).apply {
//            extensionConfig.analyticsConfiguration?.toAnalyticsConfiguration()?.let { analyticsConfiguration = it }
//            extensionConfig.poolingStrategy?.toStrategy()?.let { poolingStrategy = it }
//            extensionConfig.shardingStrategy?.toStrategy()?.let { shardingStrategy = it }
//            extensionConfig.sortingStrategy?.toStrategy()?.let { sortingStrategy = it }
//            extensionConfig.batchingStrategy?.toStrategy()?.let { batchingStrategy = it }
//            extensionConfig.flakinessStrategy?.toStrategy()?.let { flakinessStrategy = it }
//            extensionConfig.retryStrategy?.toStrategy()?.let { retryStrategy = it }
//            extensionConfig.filteringConfiguration?.toFilteringConfiguration()?.let { filteringConfiguration = it }
//            extensionConfig.ignoreFailures?.let { ignoreFailures = it }
//            extensionConfig.isCodeCoverageEnabled?.let { isCodeCoverageEnabled = it }
//            extensionConfig.fallbackToScreenshots?.let { fallbackToScreenshots = it }
//            extensionConfig.strictMode?.let { strictMode = it }
//            extensionConfig.uncompletedTestRetryQuota?.let { uncompletedTestRetryQuota = it }
//            extensionConfig.testClassRegexes?.map { it.toRegex() }?.let { testClassRegexes = it }
//            extensionConfig.includeSerialRegexes?.map { it.toRegex() }?.let { includeSerialRegexes = it }
//            extensionConfig.excludeSerialRegexes?.map { it.toRegex() }?.let { excludeSerialRegexes = it }
//            extensionConfig.testBatchTimeoutMillis?.let { testBatchTimeoutMillis = it }
//            extensionConfig.testOutputTimeoutMillis?.let { testOutputTimeoutMillis = it }
//            extensionConfig.debug?.let { debug = it }
//            extensionConfig.screenRecordingPolicy?.let { screenRecordingPolicy = it }
//            extensionConfig.analyticsTracking?.let { analyticsTracking = it }
//            extensionConfig.deviceInitializationTimeoutMillis?.let { deviceInitializationTimeoutMillis = deviceInitializationTimeoutMillis }
//            extensionConfig.outputConfiguration?.toStrategy()?.let { outputConfiguration = it }
//        }
    .build(androidConfiguration)

        //Write a Marathonfile
        val configurationFactory = ConfigurationFactory(marathonfileDir = temporaryDir)
        val yaml = configurationFactory.serialize(cnf)
        marathonfile.get().asFile.writeText(yaml)
    }

    private fun createAndroid(
        extension: MarathonExtension,
        bundles: List<GradleAndroidTestBundle>,
        flavorName: String,
        sdk: Provider<Directory>
    ): VendorConfiguration.AndroidConfiguration {
        val autoGrantPermission = DEFAULT_AUTO_GRANT_PERMISSION
        val instrumentationArgs = emptyMap<String, String>()
        val applicationPmClear = DEFAULT_APPLICATION_PM_CLEAR
        val testApplicationPmClear = DEFAULT_APPLICATION_PM_CLEAR
        val adbInitTimeout = DEFAULT_INIT_TIMEOUT_MILLIS
        val installOptions = DEFAULT_INSTALL_OPTIONS
        val screenRecordConfiguration = ScreenRecordConfiguration()
        val serialStrategy = SerialStrategy.AUTOMATIC
        val waitForDevicesTimeoutMillis = DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
        val allureConfiguration = AllureConfiguration()
        val disableWindowAnimation = DEFAULT_DISABLE_WINDOW_ANIMATION

        val outputs = bundles.map {
            val application = if (it.artifactLoader != null && it.apkFolder != null) {
                val artifactLoader = it.artifactLoader.get()
                val artifacts: BuiltArtifacts =
                    artifactLoader.load(it.apkFolder.get()) ?: throw RuntimeException("No application artifact found")
                when {
                    artifacts.elements.size > 1 -> throw UnsupportedOperationException(
                        "The Marathon plugin does not support abi splits for app APKs, " +
                            "but supports testing via a universal APK. "
                            + "Add the flag \"universalApk true\" in the android.splits.abi configuration."
                    )

                    artifacts.elements.isEmpty() -> throw UnsupportedOperationException("No artifacts for variant $flavorName")
                }
                File(artifacts.elements.first().outputFile)
            } else null

            val testArtifactsLoader = it.testArtifactLoader.get()
            val testArtifacts =
                testArtifactsLoader.load(it.testApkFolder.get()) ?: throw RuntimeException("No test artifacts for variant $flavorName")
            when {
                testArtifacts.elements.size > 1 -> throw UnsupportedOperationException("The Marathon plugin does not support abi/density splits for test APKs")
                testArtifacts.elements.isEmpty() -> throw UnsupportedOperationException("No test artifacts for variant $flavorName")
            }
            val testApplication = File(testArtifacts.elements.first().outputFile)

            AndroidTestBundleConfiguration(
                application = application,
                testApplication = testApplication,
                extraApplications = emptyList(),
            )
        }

        return VendorConfiguration.AndroidConfiguration(
            vendor = VendorConfiguration.AndroidConfiguration.VendorType.ADAM,
            androidSdk = sdk.get().asFile,
            applicationOutput = null,
            testApplicationOutput = null,
            extraApplicationsOutput = null,
            outputs = outputs,
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
            fileSyncConfiguration = FileSyncConfiguration(),
            testParserConfiguration = TestParserConfiguration.LocalTestParserConfiguration,
            testAccessConfiguration = TestAccessConfiguration(),
            timeoutConfiguration = TimeoutConfiguration(),
            adbServers = listOf(AdbEndpoint()),
            disableWindowAnimation = disableWindowAnimation,
        )
    }

    companion object {
        const val GROUP = "marathon"
    }
}
