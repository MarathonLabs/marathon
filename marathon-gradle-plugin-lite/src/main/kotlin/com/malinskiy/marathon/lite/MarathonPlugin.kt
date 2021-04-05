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
import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.exceptions.ExceptionsReporterFactory
import com.malinskiy.marathon.log.MarathonLogging
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin.VERIFICATION_GROUP
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register

private val log = MarathonLogging.logger {}
private typealias GenericCommonExtension = CommonExtension<AndroidSourceSet, BuildFeatures, BuildType, DefaultConfig, ProductFlavor, SigningConfig, Variant<VariantProperties>, VariantProperties>

private const val TASK_PREFIX = "marathon"

class MarathonPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        log.info { "Applying marathon plugin" }
        project.afterEvaluate {
            val marathonExtension = project.extensions.create<MarathonExtension>("marathon")
            val androidExtension = project.extensions.getByName<GenericCommonExtension>("android")
            val exceptionsReporter = ExceptionsReporterFactory.get(marathonExtension.bugsnag.getOrElse(false))
            exceptionsReporter.start(AppType.GRADLE_PLUGIN)
            androidExtension.onVariants {
                androidTestProperties {
                    val testVariantProperties = this
                    project.tasks.register<MarathonRunTask>("$TASK_PREFIX${testVariantProperties.name.capitalize()}") {
                        group = VERIFICATION_GROUP
                        description = "Runs instrumentation tests on all the connected devices for '${testVariantProperties.name}' " +
                            "variation and generates a report with screenshots"
                        flavorName.set(testVariantProperties.name)
                        val testedVariantProperties = testVariantProperties.testedVariant
                        builtArtifactsLoader.set(testedVariantProperties.artifacts.getBuiltArtifactsLoader())
                        apkFolder.set(testedVariantProperties.artifacts.get(ArtifactType.APK))

                        instrumentationApkDir.set(testVariantProperties.artifacts.get(ArtifactType.APK))

                        sdk.set(androidExtension.sdkComponents.sdkDirectory.get())
                        extension.set(marathonExtension)
                        this.exceptionsTracker.set(exceptionsReporter)
                    }
                }
            }
        }
    }
}
