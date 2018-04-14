package com.malinskiy.marathon

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.VerificationTask


open class MarathonRunTask : DefaultTask(), VerificationTask {
    /**
     * If true then test failures do not cause a build failure.
     */
    private var ignoreFailures: Boolean = false
//    /**
//     * Instrumentation APK.
//     */
//    @InputFile
//    var instrumentationApk: File? = null
//    /**
//     * Application APK.
//     */
//    @InputFile
//    var applicationApk: File? = null
//    /**
//     * Output directory.
//     */
//    @OutputDirectory
//    var output: File? = null
//
//    var title: String? = null
//    var subtitle: String? = null
//    var testClassRegex: String? = null
//    var testPackage: String? = null
//    var isCoverageEnabled: Boolean = false
//    var testOutputTimeout: Int = 0
//    var testSize: String? = null
//    var excludedSerials: Collection<String>? = null
//    var fallbackToScreenshots: Boolean = false
//    var totalAllowedRetryQuota: Int = 0
//    var retryPerTestCaseQuota: Int = 0
//    //    var poolingStrategy: PoolingStrategy? = null
//    var autoGrantPermissions: Boolean = false
//    var excludedAnnotation: String? = null
//    var includedAnnotation: String? = null
//    //    var sortingStrategy: SortingStrategy? = null
////    var batchStrategy: BatchStrategy? = null
////    var customExecutionStrategy: CustomExecutionStrategy? = null
//    var ignoreFailedTests: Boolean? = null
//
//    @TaskAction
//    fun runFork() {
//        log.info { "Run instrumentation tests $instrumentationApk for app $applicationApk" }
//        log.debug { "Output: $output" }
//        log.debug { "Ignore failures: $ignoreFailures" }
//
//        val configuration = invokeMethod("configuration", arrayOfNulls<Any>(0)).invokeMethod("withAndroidSdk", arrayOf<Any>(project.android.sdkDirectory)).invokeMethod("withApplicationApk", arrayOf<Any>(applicationApk)).invokeMethod("withInstrumentationApk", arrayOf<Any>(instrumentationApk)).invokeMethod("withOutput", arrayOf<Any>(output)).invokeMethod("withTitle", arrayOf<Any>(title)).invokeMethod("withSubtitle", arrayOf<Any>(subtitle)).invokeMethod("withTestClassRegex", arrayOf<Any>(testClassRegex)).invokeMethod("withTestPackage", arrayOf<Any>(testPackage)).invokeMethod("withTestOutputTimeout", arrayOf<Any>(testOutputTimeout)).invokeMethod("withTestSize", arrayOf<Any>(testSize)).invokeMethod("withExcludedSerials", arrayOf<Any>(excludedSerials)).invokeMethod("withFallbackToScreenshots", arrayOf<Any>(fallbackToScreenshots)).invokeMethod("withTotalAllowedRetryQuota", arrayOf<Any>(totalAllowedRetryQuota)).invokeMethod("withRetryPerTestCaseQuota", arrayOf<Any>(retryPerTestCaseQuota)).invokeMethod("withCoverageEnabled", arrayOf<Any>(isCoverageEnabled)).invokeMethod("withPoolingStrategy", arrayOf<Any>(poolingStrategy)).invokeMethod("withAutoGrantPermissions", arrayOf<Any>(autoGrantPermissions)).invokeMethod("withExcludedAnnotation", arrayOf<Any>(excludedAnnotation)).invokeMethod("withIncludedAnnotation", arrayOf<Any>(includedAnnotation)).invokeMethod("withSortingStrategy", arrayOf<Any>(sortingStrategy)).invokeMethod("withBatchStrategy", arrayOf<Any>(batchStrategy)).invokeMethod("withCustomExecutionStrategy", arrayOf<Any>(customExecutionStrategy)).invokeMethod("build", arrayOfNulls<Any>(0))
//
//        val success = Fork(configuration).invokeMethod("run", arrayOfNulls<Any>(0))
//        if (ignoreFailedTests == null || (!ignoreFailedTests)!!) {
//            if (!success && !ignoreFailures) {
//                throw GradleException("Tests failed! See " + output.toString() + "/html/index.html")
//            }
//
//        }
//
//    }
//
    override fun getIgnoreFailures(): Boolean {
        return ignoreFailures
    }
//
//    fun isIgnoreFailures(): Boolean {
//        return ignoreFailures
//    }
//
    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        this.ignoreFailures = ignoreFailures
    }
//
//    fun isIsCoverageEnabled(): Boolean {
//        return isCoverageEnabled
//    }
//
//    fun isFallbackToScreenshots(): Boolean {
//        return fallbackToScreenshots
//    }
//
//    fun isAutoGrantPermissions(): Boolean {
//        return autoGrantPermissions
//    }
//
//    companion object {
//        private val log = KotlinLogging.logger {}
//    }
}