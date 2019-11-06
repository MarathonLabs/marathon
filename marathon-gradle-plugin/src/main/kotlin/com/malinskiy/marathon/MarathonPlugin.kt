package com.malinskiy.marathon

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.extensions.executeGradleCompat
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.properties.MarathonProperties
import com.malinskiy.marathon.properties.marathonProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.closureOf
import java.io.File

private val log = MarathonLogging.logger {}

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        log.info { "Applying marathon plugin" }

        val properties = project.rootProject.marathonProperties
        if (properties.isCommonWorkerEnabled) {
            if (project.rootProject.extensions.findByName(EXTENSION_NAME) == null) {
                project.rootProject.extensions.create(EXTENSION_NAME, MarathonExtension::class.java, project)

                project.gradle.taskGraph.addTaskExecutionGraphListener { graph ->
                    val allMarathonTasks = graph
                        .allTasks
                        .filterIsInstance<MarathonWorkerRunTask>()

                    MarathonWorkerRunTask.setExpectedTasks(allMarathonTasks)
                }

                project.gradle.buildFinished {
                    MarathonWorkerRunTask.setExpectedTasks(emptyList())
                }
            }
        }

        project.extensions.create(EXTENSION_NAME, MarathonExtension::class.java, project)

        project.afterEvaluate {
            val appPlugin = project.plugins.findPlugin(AppPlugin::class.java)
            val libraryPlugin = project.plugins.findPlugin(LibraryPlugin::class.java)

            if (appPlugin == null && libraryPlugin == null) {
                throw IllegalStateException("Android plugin is not found")
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
                log.info { "Applying marathon for $this" }
                val testTaskForVariant = createTask(this, project, properties, testedExtension.sdkDirectory)
                marathonTask.dependsOn(testTaskForVariant)
            }
        }
    }

    companion object {
        private fun createTask(
            variant: TestVariant,
            project: Project,
            properties: MarathonProperties,
            sdkDirectory: File
        ): Task {
            checkTestVariants(variant)
            val taskType = if (properties.isCommonWorkerEnabled) MarathonWorkerRunTask::class.java else MarathonRunTask::class.java
            val marathonTask = project.tasks.create("$TASK_PREFIX${variant.name.capitalize()}", taskType)

            variant.testedVariant.outputs.all {
                val testedOutput = this
                log.info { "Processing output $testedOutput" }

                checkTestedVariants(testedOutput)

                val config = createConfiguration(
                    marathonExtensionName = EXTENSION_NAME,
                    project = project,
                    properties = properties,
                    sdkDirectory = sdkDirectory,
                    flavorName = variant.name,
                    applicationVariant = variant.testedVariant,
                    testVariant = variant
                )

                marathonTask.configure(closureOf<BaseMarathonRunTask> {
                    group = JavaBasePlugin.VERIFICATION_GROUP
                    description = "Runs instrumentation tests on all the connected devices for '${variant.name}' " +
                            "variation and generates a report with screenshots"
                    configuration = config
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
                })
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
