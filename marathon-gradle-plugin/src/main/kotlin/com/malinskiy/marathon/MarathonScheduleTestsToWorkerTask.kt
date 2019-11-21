package com.malinskiy.marathon

import com.malinskiy.marathon.android.AndroidComponentInfo
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.worker.MarathonWorker
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

private val log = MarathonLogging.logger {}

open class MarathonScheduleTestsToWorkerTask : DefaultTask() {

    lateinit var componentInfo: AndroidComponentInfo

    @TaskAction
    fun scheduleTests() {
        log.info { "Scheduling instrumentation tests ${componentInfo.testApplicationOutput} for app ${componentInfo.applicationOutput}" }

        MarathonWorker.ensureStarted()
        MarathonWorker.scheduleTests(componentInfo)
    }

}
