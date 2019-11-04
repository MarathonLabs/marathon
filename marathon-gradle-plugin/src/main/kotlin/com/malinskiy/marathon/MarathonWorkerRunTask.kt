package com.malinskiy.marathon

import com.malinskiy.marathon.android.AndroidConfiguration
import org.gradle.api.tasks.TaskAction

open class MarathonWorkerRunTask : BaseMarathonRunTask() {

    @TaskAction
    fun runMarathon() {
        val androidConfiguration = configuration.vendorConfiguration as? AndroidConfiguration

        log.info { "Scheduling instrumentation tests ${androidConfiguration?.testApplicationOutput} for app ${androidConfiguration?.applicationOutput}" }

        // TODO: start worker if needed, convert androidConfiguration to ComponentInfo and schedule it
    }
}
