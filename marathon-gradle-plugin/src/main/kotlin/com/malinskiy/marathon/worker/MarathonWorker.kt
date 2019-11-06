package com.malinskiy.marathon.worker

import com.malinskiy.marathon.MarathonRunner
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.runBlocking
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

open class MarathonWorker @Inject constructor() : WorkAction<WorkParameters.None> {

    private val log = MarathonLogging.logger {}

    private lateinit var marathon: MarathonRunner
    private lateinit var parameters: MarathonWorkParameters

    override fun execute() = runBlocking {
        log.debug("Starting worker")

        parameters = context.parameters

        if (!parameters.properties.isWorkerAutoStartEnabled) {
            context.delayStartChannel.receive()
        }

        log.debug("Starting Marathon")

        marathon = parameters.marathonFactory()

        marathon.start()

        for (component in context.componentsChannel) {
            log.debug("Scheduling tests for $component")
            marathon.scheduleTests(component)
        }

        log.debug("Waiting for completion")
        waitForCompletionAndDispose()
    }

    private suspend fun waitForCompletionAndDispose() {
        val success = marathon.waitForCompletionAndDispose()

        resetContext()

        val shouldReportFailure = !parameters.configuration.ignoreFailures
        if (!success && shouldReportFailure) {
            throw GradleException("Tests failed! See ${parameters.configuration.outputDir}/html/index.html")
        }
    }

    private fun resetContext() {
        context = WorkerContext()
    }

    override fun getParameters(): WorkParameters.None? = null

    companion object : WorkerHandler {

        private var context: WorkerContext = WorkerContext()

        override fun accept(action: WorkerAction) = context.accept(action)

        override fun ensureWorkerStarted(
            workerExecutor: WorkerExecutor,
            parameters: () -> MarathonWorkParameters
        ) {
            context.ensureWorkerStarted(workerExecutor, parameters)
        }

    }
}
