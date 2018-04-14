package com.malinskiy.marathon

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.execution.Configuration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.closureOf

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val configuration = project.container(Configuration::class.java)
        project.extensions.add("marathon", configuration)

        project.afterEvaluate {
            val appPlugin = project.plugins.findPlugin(AppPlugin::class.java)
            val libraryPlugin = project.plugins.findPlugin(LibraryPlugin::class.java)

            if (appPlugin == null && libraryPlugin == null) {
                throw IllegalStateException("Android plugin is not found")
            }

            val marathonTask: Task = project.task(TASK_PREFIX, closureOf<Task>{
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs all the instrumentation test variations on all the connected devices"
            })

            val appExtension = extensions.findByType(AppExtension::class.java)
            val libraryExtension = extensions.findByType(LibraryExtension::class.java)

            if (appExtension == null && libraryExtension == null) {
                throw IllegalStateException("No TestedExtension is not found")
            }
            val testedExtension = appExtension ?: libraryExtension
            val defaultConfig = Configuration("config")
            defaultConfig.test = "*"
            defaultConfig.tests = "*"

            val conf = configuration.getByName("config") ?: defaultConfig

            println("Starting")
            testedExtension!!.testVariants.all {
                println("Creating for $this")
                val testTaskForVariant = createTask(this, project, conf)
                marathonTask.dependsOn(testTaskForVariant)
            }
        }
    }

    companion object {
        private fun createTask(variant: TestVariant, project: Project, configuration: Configuration): MarathonRunTask {
            checkTestVariants(variant)

            val marathonTask = project.tasks.create("$TASK_PREFIX${variant.name.capitalize()}", MarathonRunTask::class.java)
            marathonTask.apply {
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs all instrumentation test for ${variant.name.capitalize()} on all the connected devices"
            }

            println("Created $TASK_PREFIX${variant.name.capitalize()}")

            variant.testedVariant.outputs.forEach {
                checkTestedVariants(it)

//                marathonTask.configure {
//
//                }
            }

//            variant.testedVariant.outputs.all(object : Closure<Any>(null, null) {
//                fun doCall(baseVariantOutput: BaseVariantOutput) {
//                    checkTestedVariants(baseVariantOutput)
//                    forkTask.configure(object : Closure<Any>(null, null) {
//                        @JvmOverloads
//                        fun doCall(it: Any? = null): Any {
//                            val config = project.fork
//
//
//                            setProperty("description", "Runs instrumentation tests on all the connected devices for \'" + variant.name + "\' variation and generates a report with screenshots")
//                            setProperty("group", JavaBasePlugin.VERIFICATION_GROUP)
//
//                            val firstOutput = DefaultGroovyMethods.first(DefaultGroovyMethods.asList(variant.outputs))
//                            setProperty("instrumentationApk", File(firstOutput.packageApplication.outputDirectory.path + "/" + firstOutput.outputFileName))
//
//                            setProperty("title", config.title)
//                            setProperty("subtitle", config.subtitle)
//                            setProperty("testClassRegex", config.testClassRegex)
//                            setProperty("testPackage", config.testPackage)
//                            setProperty("testOutputTimeout", config.testOutputTimeout)
//                            setProperty("testSize", config.testSize)
//                            setProperty("excludedSerials", config.excludedSerials)
//                            setProperty("fallbackToScreenshots", config.fallbackToScreenshots)
//                            setProperty("totalAllowedRetryQuota", config.totalAllowedRetryQuota)
//                            setProperty("retryPerTestCaseQuota", config.retryPerTestCaseQuota)
//                            setProperty("isCoverageEnabled", config.isCoverageEnabled)
//                            setProperty("poolingStrategy", config.poolingStrategy)
//                            setProperty("autoGrantPermissions", config.autoGrantPermissions)
//                            setProperty("ignoreFailures", config.ignoreFailures)
//                            setProperty("excludedAnnotation", config.excludedAnnotation)
//                            setProperty("includedAnnotation", config.includedAnnotation)
//                            setProperty("sortingStrategy", config.sortingStrategy)
//                            setProperty("batchStrategy", config.batchStrategy)
//                            setProperty("customExecutionStrategy", config.customExecutionStrategy)
//                            setProperty("ignoreFailedTests", config.ignoreFailedTests)
//
//                            setProperty("applicationApk", File(baseVariantOutput.packageApplication.outputDirectory.path + "/" + baseVariantOutput.outputFileName))
//
//                            val baseOutputDir = config.baseOutputDir
//                            val outputBase: File
//                            if (StringGroovyMethods.asBoolean(baseOutputDir)) {
//                                outputBase = File(baseOutputDir)
//                            } else {
//                                outputBase = File(project.buildDir, "reports/fork")
//                            }
//
//                            setProperty("output", File(outputBase, variant.name))
//
//                            return DefaultGroovyMethods.invokeMethod(this@ForkPlugin, "dependsOn", arrayOf<Any>(variant.testedVariant.assemble, variant.assemble))
//                        }
//
//                    })
//                    forkTask.getOutputs().upToDateWhen(object : Closure<Boolean>(null, null) {
//                        @JvmOverloads
//                        fun doCall(it: Task? = null): Boolean {
//                            return false
//                        }
//
//                    })
//                }
//
//            })
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
                throw UnsupportedOperationException("The Marathon plugin does not support abi splits for app APKs, but supports testing via a universal APK. " + "Add the flag \"universalApk true\" in the android.splits.abi configuration.")
            }

        }

        /**
         * Task name prefix.
         */
        private val TASK_PREFIX = "marathon"

        private fun <Value> setProperty0(propOwner: Project, s: String, o: Value): Value {
            propOwner.setProperty(s, o)
            return o
        }
    }
}