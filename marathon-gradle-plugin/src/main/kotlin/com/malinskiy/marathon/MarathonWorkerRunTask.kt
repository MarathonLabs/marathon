package com.malinskiy.marathon

import com.malinskiy.marathon.android.AndroidComponentInfoExtractor
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.properties.marathonProperties
import com.malinskiy.marathon.worker.MarathonWorkParameters
import com.malinskiy.marathon.worker.MarathonWorker
import com.malinskiy.marathon.worker.WorkerAction
import com.malinskiy.marathon.worker.WorkerAction.ScheduleTests
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor

open class MarathonWorkerRunTask : BaseMarathonRunTask() {

    private val componentInfoExtractor = AndroidComponentInfoExtractor()

    @TaskAction
    fun runMarathon() {
        val androidConfiguration = configuration.vendorConfiguration as? AndroidConfiguration
        val componentInfo = componentInfoExtractor.extract(configuration)
        val properties = project.rootProject.marathonProperties

        log.info { "Scheduling instrumentation tests ${androidConfiguration?.testApplicationOutput} for app ${androidConfiguration?.applicationOutput}" }

        val workerExecutor = services.get(WorkerExecutor::class.java)

        MarathonWorker.ensureWorkerStarted(workerExecutor) {
            MarathonWorkParameters(
                configuration = configuration,
                properties = properties,
                marathonFactory = {
                    val application = marathonStartKoin(configuration)
                    application.koin.get<Marathon>()
                }
            )
        }

        MarathonWorker.accept(ScheduleTests(componentInfo))

        onFinished(this)
    }

    // TODO: refactor finishing logic
    companion object {

        private lateinit var tasks: MutableSet<MarathonWorkerRunTask>

        fun setExpectedTasks(tasks: List<MarathonWorkerRunTask>) {
            this.tasks = tasks.toMutableSet()
        }

        private fun onFinished(task: MarathonWorkerRunTask) {
            this.tasks.remove(task)

            if (this.tasks.isEmpty()) {
                MarathonWorker.accept(WorkerAction.Finish)
            }
        }
    }
}
