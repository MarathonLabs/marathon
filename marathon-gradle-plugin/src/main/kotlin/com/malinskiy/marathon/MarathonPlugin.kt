package com.malinskiy.marathon

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.dsl.SigningConfig
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantProperties
import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.exceptions.ExceptionsReporterFactory
import com.malinskiy.marathon.log.MarathonLogging
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register

private val log = MarathonLogging.logger {}

private typealias GenericCommonExtension = CommonExtension<AndroidSourceSet, BuildFeatures, BuildType, DefaultConfig, ProductFlavor, SigningConfig, Variant<VariantProperties>, VariantProperties>

private const val TASK_PREFIX = "marathon"

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        log.info { "Applying marathon plugin" }

        val extension: MarathonExtension = project.extensions.create("marathon", MarathonExtension::class.java)

        project.afterEvaluate {
            val exceptionsReporter = ExceptionsReporterFactory.get(extension.bugsnag.orNull != false)
            exceptionsReporter.start(AppType.GRADLE_PLUGIN)

            val androidExtension = project.extensions.getByName<GenericCommonExtension>("android")
            androidExtension.onVariants {
                androidTestProperties {
                    val testVariantProperties = this
                    val marathonTask = project.tasks.register<MarathonRunTask>("$TASK_PREFIX${testVariantProperties.name.capitalize()}") {
                        group = JavaBasePlugin.VERIFICATION_GROUP
                        description = "Runs instrumentation tests on all the connected devices for '${testVariantProperties.name}' " +
                            "variation and generates a report with screenshots"
                        flavorName.set(testVariantProperties.name)
                        val testedVariantProperties = testVariantProperties.testedVariant
                        builtArtifactsLoader.set(testedVariantProperties.artifacts.getBuiltArtifactsLoader())
                        apkFolder.set(testedVariantProperties.artifacts.get(ArtifactType.APK))
                        instrumentationApkDir.set(testVariantProperties.artifacts.get(ArtifactType.APK))

                        name.set(extension.name)
                        vendor.set(extension.vendor)
                        bugsnag.set(extension.bugsnag)
                        analyticsConfiguration.set(extension.analyticsConfiguration)
                        poolingStrategy.set(extension.poolingStrategy)
                        shardingStrategy.set(extension.shardingStrategy)
                        sortingStrategy.set(extension.sortingStrategy)
                        batchingStrategy.set(extension.batchingStrategy)
                        flakinessStrategy.set(extension.flakinessStrategy)
                        retryStrategy.set(extension.retryStrategy)
                        filteringConfiguration.set(extension.filteringConfiguration)

                        baseOutputDir.set(extension.baseOutputDir)
                        ignoreFailures.set(extension.ignoreFailures)
                        codeCoverageEnabled.set(extension.isCodeCoverageEnabled)
                        fallbackToScreenshots.set(extension.fallbackToScreenshots)
                        strictMode.set(extension.strictMode)
                        uncompletedTestRetryQuota.set(extension.uncompletedTestRetryQuota)

                        testClassRegexes.set(extension.testClassRegexes)
                        includeSerialRegexes.set(extension.includeSerialRegexes)
                        excludeSerialRegexes.set(extension.excludeSerialRegexes)

                        testBatchTimeoutMillis.set(extension.testBatchTimeoutMillis)
                        testOutputTimeoutMillis.set(extension.testOutputTimeoutMillis)
                        debug.set(extension.debug)

                        screenRecordingPolicy.set(extension.screenRecordingPolicy)

                        applicationPmClear.set(extension.applicationPmClear)
                        testApplicationPmClear.set(extension.testApplicationPmClear)
                        adbInitTimeout.set(extension.adbInitTimeout)
                        installOptions.set(extension.installOptions)
                        serialStrategy.set(extension.serialStrategy)

                        screenRecordConfiguration.set(extension.screenRecordConfiguration)

                        analyticsTracking.set(extension.analyticsTracking)

                        deviceInitializationTimeoutMillis.set(extension.deviceInitializationTimeoutMillis)
                        waitForDevicesTimeoutMillis.set(extension.waitForDevicesTimeoutMillis)

                        allureConfiguration.set(extension.allureConfiguration)
                        timeoutConfiguration.set(extension.timeoutConfiguration)
                        fileSyncConfiguration.set(extension.fileSyncConfiguration)

                        //Android specific for now
                        autoGrantPermission.set(extension.autoGrantPermission)
                        instrumentationArgs.set(extension.instrumentationArgs)


                        sdk.set(androidExtension.sdkComponents.sdkDirectory)
                        outputs.upToDateWhen { false }
                        exceptionsTracker.set(exceptionsReporter)
                    }
                }
            }
        }
    }
}
