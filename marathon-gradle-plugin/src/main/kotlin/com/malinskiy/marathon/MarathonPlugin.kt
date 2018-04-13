package com.malinskiy.marathon

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.android.builder.model.AndroidProject
import com.malinskiy.marathon.execution.Configuration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

class MarathonPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {
            val appPlugin = project.plugins.findPlugin(AppPlugin::class.java)
            val libraryPlugin = project.plugins.findPlugin(LibraryPlugin::class.java)

            if (appPlugin == null && libraryPlugin == null) {
                throw IllegalStateException("Android plugin is not found")
            }

            val marathon = project.container(Configuration::class.java)
            project.extensions.add("marathon", marathon)

            val marathonTask = project.task(TASK_PREFIX) {
                group = JavaBasePlugin.VERIFICATION_GROUP
                description = "Runs all the instrumentation test variations on all the connected devices"
            }

            val appExtension = extensions.findByType(AppExtension::class.java)
            val libraryExtension = extensions.findByType(LibraryExtension::class.java)

            if (appExtension == null && libraryExtension == null) {
                throw IllegalStateException("No TestedExtension is not found")
            }
            val testedExtension = appExtension ?: libraryExtension

            testedExtension!!.testVariants.all {

            }

//        val forkTask = project.task(TASK_PREFIX, object : Closure<String>(this, this) {
//            @JvmOverloads
//            fun doCall(it: Any? = null): String {
//                setProperty("group", JavaBasePlugin.VERIFICATION_GROUP)
//                return setProperty0(this@ForkPlugin, "description", "Runs all the instrumentation test variations on all the connected devices")
//            }
//
//        })
//
//        val android = project.android
//        android.testVariants.invokeMethod("all", arrayOf<Any>(object : Closure<Task>(this, this) {
//            fun doCall(variant: TestVariant): Task {
//                val forkTaskForTestVariant = ForkPlugin.createTask(variant, project)
//                return forkTask.dependsOn(forkTaskForTestVariant)
//            }
//
//        }))
        }
    }

    companion object {
//        private fun createTask(variant: TestVariant, project: Project): ForkRunTask {
//            checkTestVariants(variant)
//
//            val forkTask = project.tasks.create(TASK_PREFIX + StringGroovyMethods.capitalize(variant.name), ForkRunTask::class.java as Class<T>)
//
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
//            return forkTask
//        }

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