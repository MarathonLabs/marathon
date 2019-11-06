package com.malinskiy.marathon.worker

import org.gradle.workers.WorkerExecutor

interface WorkerHandler {
    fun accept(action: WorkerAction)
    fun ensureWorkerStarted(workerExecutor: WorkerExecutor, parameters: () -> MarathonWorkParameters)
}