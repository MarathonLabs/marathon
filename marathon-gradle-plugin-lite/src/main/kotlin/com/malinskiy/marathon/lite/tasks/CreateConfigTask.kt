package com.malinskiy.marathon.lite.tasks

import com.android.build.api.variant.BuiltArtifactsLoader
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration
import com.malinskiy.marathon.cliconfig.proto.Configuration
import com.malinskiy.marathon.cliconfig.proto.StringList
import com.malinskiy.marathon.cliconfig.proto.VendorConfiguration
import com.malinskiy.marathon.lite.configuration.AllureConfiguration
import com.malinskiy.marathon.lite.configuration.AnalyticsConfiguration
import com.malinskiy.marathon.lite.configuration.BatchingStrategy
import com.malinskiy.marathon.lite.configuration.FileSyncConfiguration
import com.malinskiy.marathon.lite.configuration.FilteringConfiguration
import com.malinskiy.marathon.lite.configuration.FlakinessStrategy
import com.malinskiy.marathon.lite.configuration.PoolingStrategy
import com.malinskiy.marathon.lite.configuration.RetryStrategy
import com.malinskiy.marathon.lite.configuration.ScreenRecordConfiguration
import com.malinskiy.marathon.lite.configuration.ScreenRecordingPolicy
import com.malinskiy.marathon.lite.configuration.SerialStrategy
import com.malinskiy.marathon.lite.configuration.ShardingStrategy
import com.malinskiy.marathon.lite.configuration.SortingStrategy
import com.malinskiy.marathon.lite.configuration.TimeoutConfiguration
import com.malinskiy.marathon.lite.configuration.VendorType
import com.malinskiy.marathon.lite.configuration.toProto
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

open class CreateConfigTask @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask() {
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

    @get:Input
    val name: Property<String> = objects.property()

    @get:Input
    val vendor: Property<VendorType> = objects.property()

    @get:Input
    val bugsnag: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val analyticsConfiguration: Property<AnalyticsConfiguration> = objects.property()

    @get:Input
    @get:Optional
    val poolingStrategy: Property<PoolingStrategy> = objects.property()

    @get:Input
    @get:Optional
    val shardingStrategy: Property<ShardingStrategy> = objects.property()

    @get:Input
    @get:Optional
    val sortingStrategy: Property<SortingStrategy> = objects.property()

    @get:Input
    @get:Optional
    val batchingStrategy: Property<BatchingStrategy> = objects.property()

    @get:Input
    @get:Optional
    val flakinessStrategy: Property<FlakinessStrategy> = objects.property()

    @get:Input
    @get:Optional
    val retryStrategy: Property<RetryStrategy> = objects.property()

    @get:Input
    @get:Optional
    val filteringConfiguration: Property<FilteringConfiguration> = objects.property()

    @get:Input
    @get:Optional
    val baseOutputDir: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val ignoreFailures: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val codeCoverageEnabled: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val fallbackToScreenshots: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val strictMode: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val uncompletedTestRetryQuota: Property<Int> = objects.property()

    @get:Input
    @get:Optional
    val testClassRegexes: ListProperty<String> = objects.listProperty(String::class.java)

    @get:Input
    @get:Optional
    val includeSerialRegexes: ListProperty<String> = objects.listProperty(String::class.java)

    @get:Input
    @get:Optional
    val excludeSerialRegexes: ListProperty<String> = objects.listProperty(String::class.java)

    @get:Input
    @get:Optional
    val testBatchTimeoutMillis: Property<Long> = objects.property()

    @get:Input
    @get:Optional
    val testOutputTimeoutMillis: Property<Long> = objects.property()

    @get:Input
    @get:Optional
    val debug: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val screenRecordingPolicy: Property<ScreenRecordingPolicy> = objects.property()

    @get:Input
    @get:Optional
    val applicationPmClear: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val testApplicationPmClear: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val adbInitTimeout: Property<Int> = objects.property()

    @get:Input
    @get:Optional
    val installOptions: Property<String> = objects.property()

    @get:Input
    @get:Optional
    val serialStrategy: Property<SerialStrategy> = objects.property()

    @get:Input
    @get:Optional
    val screenRecordConfiguration: Property<ScreenRecordConfiguration> = objects.property()

    @get:Input
    @get:Optional
    val analyticsTracking: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val deviceInitializationTimeoutMillis: Property<Long> = objects.property()

    @get:Input
    @get:Optional
    val waitForDevicesTimeoutMillis: Property<Long> = objects.property()

    @get:Input
    @get:Optional
    val allureConfiguration: Property<AllureConfiguration> = objects.property()

    @get:Input
    @get:Optional
    val timeoutConfiguration: Property<TimeoutConfiguration> = objects.property()

    @get:Input
    @get:Optional
    val fileSyncConfiguration: Property<FileSyncConfiguration> = objects.property()

    //Android specific for now
    @get:Input
    @get:Optional
    val autoGrantPermissions: Property<Boolean> = objects.property()

    @get:Input
    @get:Optional
    val instrumentationArgs: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)


    @get:OutputFile
    val configOutput: Provider<RegularFile> = projectLayout.buildDirectory.file("Marathonfile.protobinary");

    @TaskAction
    fun createConfig() {
        val instrumentationApk = instrumentationApkDir.asFileTree.single { it.extension == "apk" }
        val buildArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
        val applicationApk = buildArtifacts?.elements?.first()?.outputFile
        val baseOutputDir = File(baseOutputDir.getOrElse(project.buildDir.toString()), "reports/marathon")
        val output = File(baseOutputDir, flavorName.get())

        val vendorConfiguration = createAndroidConfiguration(applicationApk?.let { File(it) }, instrumentationApk)
        val builder = Configuration.newBuilder()
        analyticsConfiguration.orNull?.let { builder.setAnalyticsConfiguration(it.toProto()) }
        poolingStrategy.orNull?.let { builder.setPoolingStrategy(it.toProto()) }
        shardingStrategy.orNull?.let { builder.setShardingStrategy(it.toProto()) }
        sortingStrategy.orNull?.let { builder.setSortingStrategy(it.toProto()) }
        batchingStrategy.orNull?.let { builder.setBatchingStrategy(it.toProto()) }
        flakinessStrategy.orNull?.let { builder.setFlakinessStrategy(it.toProto()) }
        retryStrategy.orNull?.let { builder.setRetryStrategy(it.toProto()) }
        filteringConfiguration.orNull?.let { builder.setFilteringConfiguration(it.toProto()) }

        ignoreFailures.orNull?.let { builder.setIgnoreFailures(it) }
        codeCoverageEnabled.orNull?.let { builder.setIsCodeCoverageEnabled(it) }
        fallbackToScreenshots.orNull?.let { builder.setFallbackToScreenshots(it) }
        strictMode.orNull?.let { builder.setStrictMode(it) }
        uncompletedTestRetryQuota.orNull?.let { builder.setUncompletedTestRetryQuota(it) }
        testClassRegexes.orNull?.let { builder.setTestClassRegexes(StringList.newBuilder().addAllValues(it).build()) }
        includeSerialRegexes.orNull?.let { builder.setIncludeSerialRegexes(StringList.newBuilder().addAllValues(it).build()) }
        excludeSerialRegexes.orNull?.let { builder.setExcludeSerialRegexes(StringList.newBuilder().addAllValues(it).build()) }

        testBatchTimeoutMillis.orNull?.let { builder.setTestBatchTimeoutMillis(it) }
        testOutputTimeoutMillis.orNull?.let { builder.setTestOutputTimeoutMillis(it) }
        debug.orNull?.let { builder.setDebug(it) }
        screenRecordingPolicy.orNull?.let { builder.setScreenRecordingPolicy(it.toProto()) }
        builder.vendorConfiguration = vendorConfiguration
        analyticsTracking.orNull?.let { builder.setAnalyticsTracking(it) }
        deviceInitializationTimeoutMillis.orNull?.let { builder.setDeviceInitializationTimeoutMillis(it) }

        val cnf = builder.build()
        saveConfig(cnf, configOutput.get().asFile)
    }

    private fun saveConfig(config: Configuration, output: File) {
        config.writeTo(output.outputStream())
    }

    private fun createAndroidConfiguration(
        applicationApk: File?,
        instrumentationApk: File
    ): VendorConfiguration {
        val builder = AndroidConfiguration.newBuilder()
            .setAndroidSdk(sdk.get().asFile.toString())
            .setTestApplicationApk(instrumentationApk.absolutePath)

        applicationApk?.let { builder.setApplicationApk(it.absolutePath) }
        autoGrantPermissions.orNull?.let { builder.setAutoGrantPermission(it) }
        instrumentationArgs.orNull?.let { builder.putAllInstrumentationArgs(it) }
        applicationPmClear.orNull?.let { builder.setApplicationPmClear(it) }
        testApplicationPmClear.orNull?.let { builder.setTestApplicationPmClear(it) }
        adbInitTimeout.orNull?.let { builder.setAdbInitTimeoutMillis(it) }
        installOptions.orNull?.let { builder.setInstallOptions(it) }
        screenRecordConfiguration.orNull?.let { builder.setScreenRecordConfiguration(it.toProto()) }
        serialStrategy.orNull?.let { builder.setSerialStrategy(it.toProto()) }
        waitForDevicesTimeoutMillis.orNull?.let { builder.setWaitForDevicesTimeoutMillis(it) }
        allureConfiguration.orNull?.let { builder.setAllureConfiguration(it.toProto()) }
        val androidConfiguration = builder.build()

        return VendorConfiguration.newBuilder().setAndroid(androidConfiguration).build()
    }
}
