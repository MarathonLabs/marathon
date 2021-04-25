package com.malinskiy.marathon.lite

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
import com.malinskiy.marathon.lite.tasks.CreateConfigTask
import com.malinskiy.marathon.lite.tasks.MarathonRunTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin.VERIFICATION_GROUP
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import kotlin.random.Random

private typealias GenericCommonExtension = CommonExtension<AndroidSourceSet, BuildFeatures, BuildType, DefaultConfig, ProductFlavor, SigningConfig, Variant<VariantProperties>, VariantProperties>

private const val TASK_PREFIX = "marathon"

class MarathonPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val marathonExtension = project.extensions.create<MarathonExtension>("marathon")
        project.afterEvaluate {

            val androidExtension = project.extensions.getByName<GenericCommonExtension>("android")
            androidExtension.onVariants {
                androidTestProperties {
                    val testVariantProperties = this
                    val generateConfigTask =
                        project.tasks.register<CreateConfigTask>("generate${TASK_PREFIX.capitalize()}Config${testVariantProperties.name.capitalize()}") {
                            group = VERIFICATION_GROUP
                            description = "Generate marathon configuration file for '${testVariantProperties.name}'"
                            flavorName.set(testVariantProperties.name)
                            val testedVariantProperties = testVariantProperties.testedVariant
                            builtArtifactsLoader.set(testedVariantProperties.artifacts.getBuiltArtifactsLoader())
                            apkFolder.set(testedVariantProperties.artifacts.get(ArtifactType.APK))
                            instrumentationApkDir.set(testVariantProperties.artifacts.get(ArtifactType.APK))
                            sdk.set(androidExtension.sdkComponents.sdkDirectory.get())
                            name.set(marathonExtension.name)
                            vendor.set(marathonExtension.vendor)
                            baseOutputDir.set(marathonExtension.baseOutputDir)
                            bugsnag.set(marathonExtension.bugsnag)
                            analyticsConfiguration.set(marathonExtension.analyticsConfiguration)
                            poolingStrategy.set(marathonExtension.poolingStrategy)
                            shardingStrategy.set(marathonExtension.shardingStrategy)
                            sortingStrategy.set(marathonExtension.sortingStrategy)
                            batchingStrategy.set(marathonExtension.batchingStrategy)
                            flakinessStrategy.set(marathonExtension.flakinessStrategy)
                            retryStrategy.set(marathonExtension.retryStrategy)
                            filteringConfiguration.set(marathonExtension.filteringConfiguration)
                            baseOutputDir.set(marathonExtension.baseOutputDir)
                            ignoreFailures.set(marathonExtension.ignoreFailures)
                            codeCoverageEnabled.set(marathonExtension.isCodeCoverageEnabled)
                            fallbackToScreenshots.set(marathonExtension.fallbackToScreenshots)
                            strictMode.set(marathonExtension.strictMode)
                            uncompletedTestRetryQuota.set(marathonExtension.uncompletedTestRetryQuota)
                            testClassRegexes.set(marathonExtension.testClassRegexes)
                            includeSerialRegexes.set(marathonExtension.includeSerialRegexes)
                            excludeSerialRegexes.set(marathonExtension.excludeSerialRegexes)
                            testBatchTimeoutMillis.set(marathonExtension.testBatchTimeoutMillis)
                            testOutputTimeoutMillis.set(marathonExtension.testOutputTimeoutMillis)
                            debug.set(marathonExtension.debug)
                            screenRecordingPolicy.set(marathonExtension.screenRecordingPolicy)
                            applicationPmClear.set(marathonExtension.applicationPmClear)
                            testApplicationPmClear.set(marathonExtension.testApplicationPmClear)
                            adbInitTimeout.set(marathonExtension.adbInitTimeout)
                            installOptions.set(marathonExtension.installOptions)
                            serialStrategy.set(marathonExtension.serialStrategy)
                            screenRecordConfiguration.set(marathonExtension.screenRecordConfiguration)
                            analyticsTracking.set(marathonExtension.analyticsTracking)
                            deviceInitializationTimeoutMillis.set(marathonExtension.deviceInitializationTimeoutMillis)
                            waitForDevicesTimeoutMillis.set(marathonExtension.waitForDevicesTimeoutMillis)
                            allureConfiguration.set(marathonExtension.allureConfiguration)
                            timeoutConfiguration.set(marathonExtension.timeoutConfiguration)
                            fileSyncConfiguration.set(marathonExtension.fileSyncConfiguration)
                            autoGrantPermissions.set(marathonExtension.autoGrantPermissions)
                            instrumentationArgs.set(marathonExtension.instrumentationArgs)
                        }
                    val runTask = project.tasks.register<MarathonRunTask>("$TASK_PREFIX${testVariantProperties.name.capitalize()}") {
                        group = VERIFICATION_GROUP
                        description = "Runs instrumentation tests on all the connected devices for '${testVariantProperties.name}' " +
                            "variation and generates a report with screenshots"
                        marathonConfigFile.set(generateConfigTask.get().configOutput)
                        randomProperty.set(Random.nextInt())
                        dependsOn(generateConfigTask)
                    }
                }
            }
        }
    }
}
