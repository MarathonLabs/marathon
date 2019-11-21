package com.malinskiy.marathon.worker

import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.MarathonRunner
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.gradle.api.GradleException

class WorkerRunnable(
    private val componentsChannel: Channel<ComponentInfo>,
    private val configuration: Configuration
) : Runnable {

    private val log = MarathonLogging.logger {}

    private lateinit var marathon: MarathonRunner

    override fun run() = runBlocking {
        log.debug("Starting Marathon worker")

        val application = marathonStartKoin(configuration)
        marathon = application.koin.get<Marathon>()

        // TODO: we need to support empty queue in Marathon, currently it fails if we start it without tests
        val firstComponent = componentsChannel.receive()
        marathon.start()
        marathon.scheduleTests(firstComponent)

        for (component in componentsChannel) {
            log.debug("Scheduling tests for $component")
            marathon.scheduleTests(component)
        }

        log.debug("Waiting for completion")
        waitForCompletionAndDispose()
    }

    private suspend fun waitForCompletionAndDispose() {
        val success = marathon.waitForCompletionAndDispose()

        val shouldReportFailure = !configuration.ignoreFailures
        if (!success && shouldReportFailure) {
            throw GradleException("Tests failed! See ${configuration.outputDir}/html/index.html")
        }
    }
}
