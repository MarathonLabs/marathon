package com.malinskiy.marathon

import com.malinskiy.marathon.analytics.TrackActionType
import com.malinskiy.marathon.analytics.UsageAnalytics
import com.malinskiy.marathon.analytics.tracker.Event
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

private val log = MarathonLogging.logger {}

open class MarathonRunTask : DefaultTask(), VerificationTask {

    var configuration: Configuration? = null

    @TaskAction
    fun runMarathon() {
        val cnf = configuration!!
        val androidConfiguration = cnf.vendorConfiguration as? AndroidConfiguration

        log.info { "Run instrumentation tests ${androidConfiguration?.testApplicationOutput} for app ${androidConfiguration?.applicationOutput}" }
        log.debug { "Output: ${cnf.outputDir}" }
        log.debug { "Ignore failures: ${cnf.ignoreFailures}" }

        UsageAnalytics.enable = cnf.analyticsTracking
        UsageAnalytics.tracker.trackEvent(Event(TrackActionType.RunType, "gradle"))

        val success = Marathon(cnf).run()

        if (!success && !cnf.ignoreFailures) {
            throw GradleException("Tests failed! See ${cnf.outputDir}/html/index.html")
        }

    }

    override fun getIgnoreFailures(): Boolean {
        return configuration!!.ignoreFailures
    }

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        configuration = configuration!!.copy(ignoreFailures = ignoreFailures)
    }
}
