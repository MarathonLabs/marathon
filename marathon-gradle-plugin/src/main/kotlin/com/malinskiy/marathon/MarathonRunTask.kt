package com.malinskiy.marathon

import com.malinskiy.marathon.execution.Configuration
import mu.KotlinLogging
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

private val log = KotlinLogging.logger {}

open class MarathonRunTask : DefaultTask(), VerificationTask {

    var configuration: Configuration? = null

    @TaskAction
    fun runFork() {
        val cnf = configuration!!

        log.info { "Run instrumentation tests ${cnf.testApplicationOutput} for app ${cnf.applicationOutput}" }
        log.debug { "Output: ${cnf.outputDir}" }
        log.debug { "Ignore failures: ${cnf.ignoreFailures}" }

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