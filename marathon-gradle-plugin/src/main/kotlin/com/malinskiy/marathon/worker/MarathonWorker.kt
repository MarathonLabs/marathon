package com.malinskiy.marathon.worker

import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.execution.Configuration

object MarathonWorker : WorkerHandler {

    private var context: WorkerContext = WorkerContext()

    override fun initialize(configuration: Configuration) = context.initialize(configuration)

    override fun ensureStarted() = context.ensureStarted()

    override fun scheduleTests(componentInfo: ComponentInfo) = context.scheduleTests(componentInfo)

    override fun stop() {
        try {
            context.stop()
        } finally {
            // re-create context to clear the reference for future runs
            context = WorkerContext()
        }
    }
}
