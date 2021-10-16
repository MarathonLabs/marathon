package com.malinskiy.marathon.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.gradle.extensions.executeGradleCompat
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.closureOf
import java.io.File

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val logger = project.logger
        logger.info("Applying marathon plugin")
        val extension: MarathonExtension = project.extensions.create("marathon", MarathonExtension::class.java, project)

        project.afterEvaluate {
            val cli = prepareDistribution(project)

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

            val conf = extensions.getByName("marathon") as? MarathonExtension ?: MarathonExtension(project)

            testedExtension!!.testVariants.all {
                logger.info("Applying marathon for ${this.baseName}")
                val testTaskForVariant = createTask(logger, this, project, conf, testedExtension.sdkDirectory, cli)
                marathonTask.dependsOn(testTaskForVariant)
            }
        }
    }

    private fun prepareDistribution(project: Project): File {
        val buildDir = project.buildDir
        val marathonBuildDir = File(buildDir, "marathon").apply { mkdirs() }

        return DistributionInstaller().install(marathonBuildDir)
    }

    companion object {
        private fun createTask(
            logger: Logger,
            variant: TestVariant,
            project: Project,
            config: MarathonExtension,
            sdkDirectory: File,
            cli: File,
        ): MarathonRunTask {
            checkTestVariants(variant)

            val marathonTask = project.tasks.create("$TASK_PREFIX${variant.name.capitalize()}", MarathonRunTask::class.java)

            variant.testedVariant.outputs.all {
                val testedOutput = this
                logger.info("Processing output $testedOutput")

                checkTestedVariants(testedOutput)
                marathonTask.configure(closureOf<MarathonRunTask> {
                    group = JavaBasePlugin.VERIFICATION_GROUP
                    description = "Runs instrumentation tests on all the connected devices for '${variant.name}' " +
                        "variation and generates a report with screenshots"
                    flavorName.set(variant.name)
                    applicationVariant.set(variant.testedVariant)
                    testVariant.set(variant)
                    marathonExtension.set(config)
                    sdk.set(sdkDirectory)
                    outputs.upToDateWhen { false }
                    executeGradleCompat(
                        exec = {
                            dependsOn(variant.testedVariant.assembleProvider, variant.assembleProvider)
                        },
                        fallbacks = listOf {
                            @Suppress("DEPRECATION")
                            dependsOn(variant.testedVariant.assemble, variant.assemble)
                        }
                    )
                    executable(cli.absolutePath)
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
    }
}
