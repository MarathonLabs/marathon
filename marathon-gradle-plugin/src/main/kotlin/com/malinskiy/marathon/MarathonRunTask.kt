package com.malinskiy.marathon

import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import java.io.File

private val log = MarathonLogging.logger {}

open class MarathonRunTask : DefaultTask(), VerificationTask {

    lateinit var configuration: Configuration

    var ignoreFailure: Boolean = false

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")

    @TaskAction
    fun runMarathon() {
        val androidConfiguration = configuration.vendorConfiguration as? AndroidConfiguration

        log.info { "Run instrumentation tests ${androidConfiguration?.testApplicationOutput} for app ${androidConfiguration?.applicationOutput}" }
        log.debug { "Output: ${configuration.outputDir}" }
        log.debug { "Ignore failures: ${configuration.ignoreFailures}" }

        UsageAnalytics.enable = configuration.analyticsTracking
        UsageAnalytics.USAGE_TRACKER.trackEvent(Event(TrackActionType.RunType, "gradle"))

        val application = marathonStartKoin(configuration)
        val marathon: Marathon = application.koin.get()

        val success = marathon.run()

        val shouldReportFailure = !configuration.ignoreFailures
        if (!success && shouldReportFailure) {
            throw GradleException("Tests failed! See ${configuration.outputDir}/html/index.html")
        }
    }

    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }
}
