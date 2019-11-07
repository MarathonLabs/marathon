package com.malinskiy.marathon.worker

import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.properties.marathonProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor

open class StartWorkerTask : DefaultTask() {

    lateinit var configuration: Configuration

    @TaskAction
    fun start() {
        val properties = project.rootProject.marathonProperties
        val workerExecutor = services.get(WorkerExecutor::class.java)

        MarathonWorker.startWorker(workerExecutor) {
            MarathonWorkParameters(
                configuration = configuration,
                properties = properties,
                marathonFactory = {
                    val application = marathonStartKoin(configuration)
                    application.koin.get<Marathon>()
                }
            )
        }
    }
}
