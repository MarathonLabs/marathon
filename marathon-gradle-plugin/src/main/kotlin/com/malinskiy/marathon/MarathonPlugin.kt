package com.malinskiy.marathon

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.android.androidSdkLocation
import com.malinskiy.marathon.extensions.executeGradleCompat
import com.malinskiy.marathon.properties.MarathonProperties
import com.malinskiy.marathon.properties.marathonProperties
import com.malinskiy.marathon.worker.MarathonWorker
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.closureOf
import java.io.File

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val properties = project.rootProject.marathonProperties
        val androidSdkLocation = project.androidSdkLocation

        if (properties.isCommonWorkerEnabled) {
            project.setUpWorker()
        }

        if (project.extensions.findByName(EXTENSION_NAME) == null) {
            project.extensions.create(EXTENSION_NAME, MarathonExtension::class.java, project)
        }

        project.afterEvaluate {
            val appPlugin = project.plugins.findPlugin(AppPlugin::class.java)
            val libraryPlugin = project.plugins.findPlugin(LibraryPlugin::class.java)

            if (appPlugin == null && libraryPlugin == null) {
                if (project != rootProject) {
                    throw IllegalStateException("Android plugin is not found")
                } else {
                    return@afterEvaluate
                }
            }

            val marathonTask: Task = project.task(TASK_PREFIX, closureOf<Task> {
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs all the instrumentation test variations on all the connected devices"
            })

            val appExtension = extensions.findByType(AppExtension::class.java)
            val libraryExtension = extensions.findByType(LibraryExtension::class.java)

            if (appExtension == null && libraryExtension == null) {
                throw IllegalStateException("No TestedExtension is found")
            }
            val testedExtension = appExtension ?: libraryExtension

            testedExtension!!.testVariants.all {
                val testTaskForVariant = registerTask(this, project, properties, androidSdkLocation)
                marathonTask.dependsOn(testTaskForVariant)
            }
        }
    }

    companion object {

        private fun Project.setUpWorker() {
            if (project.rootProject.extensions.findByName(EXTENSION_NAME) == null) {
                project.rootProject.extensions.create(EXTENSION_NAME, MarathonExtension::class.java, project.rootProject)

                gradle.projectsEvaluated {
                    val configuration = createCommonConfiguration(project.rootProject, EXTENSION_NAME, androidSdkLocation)
                    MarathonWorker.initialize(configuration)
                }

                gradle.buildFinished {
                    MarathonWorker.stop()
                }
            }
        }

        private fun registerTask(
            variant: TestVariant,
            project: Project,
            properties: MarathonProperties,
            sdkDirectory: File
        ): TaskProvider<out DefaultTask> {
            checkTestVariants(variant)

            val taskType =
                if (properties.isCommonWorkerEnabled) MarathonScheduleTestsToWorkerTask::class.java else MarathonRunTask::class.java
            val marathonTask = project.tasks.register("$TASK_PREFIX${variant.name.capitalize()}", taskType)

            marathonTask.configure {
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs instrumentation tests on all the connected devices for '${variant.name}' " +
                        "variation and generates a report with screenshots"
                outputs.upToDateWhen { false }

                executeGradleCompat(
                    exec = {
                        dependsOn(variant.testedVariant.assembleProvider, variant.assembleProvider)
                    },
                    fallback = {
                        @Suppress("DEPRECATION")
                        dependsOn(variant.testedVariant.assemble, variant.assemble)
                    }
                )
            }

            variant.testedVariant.outputs.all {
                val testedOutput = this

                checkTestedVariants(testedOutput)

                if (properties.isCommonWorkerEnabled) {
                    val componentInfo = createComponentInfo(
                        project = project,
                        flavorName = variant.name,
                        applicationVariant = variant.testedVariant,
                        testVariant = variant
                    )

                    marathonTask.configure {
                        (this as MarathonScheduleTestsToWorkerTask).componentInfo = componentInfo
                    }
                } else {
                    val config = createConfiguration(
                        marathonExtensionName = EXTENSION_NAME,
                        project = project,
                        sdkDirectory = sdkDirectory,
                        flavorName = variant.name,
                        applicationVariant = variant.testedVariant,
                        testVariant = variant
                    )

                    marathonTask.configure {
                        (this as MarathonRunTask).configuration = config
                    }
                }
            }

            return marathonTask
        }

        private fun checkTestVariants(testVariant: TestVariant) {
            if (testVariant.outputs.size > 1) {
                throw UnsupportedOperationException("The Marathon plugin does not support abi/density splits for test APKs")
            }

        }

        /**
         * Checks that if the base variant contains more than one outputs (and has therefore splits), it is the universal APK.
         * Otherwise, we can test the single output. This is a workaround until Fork supports test & app splits properly.
         *
         * @param baseVariant the tested variant
         */
        private fun checkTestedVariants(baseVariantOutput: BaseVariantOutput) {
            if (baseVariantOutput.outputs.size > 1) {
                throw UnsupportedOperationException(
                    "The Marathon plugin does not support abi splits for app APKs, " +
                            "but supports testing via a universal APK. "
                            + "Add the flag \"universalApk true\" in the android.splits.abi configuration."
                )
            }

        }

        /**
         * Task name prefix.
         */
        private const val TASK_PREFIX = "marathon"

        private const val EXTENSION_NAME = "marathon"
    }
}
