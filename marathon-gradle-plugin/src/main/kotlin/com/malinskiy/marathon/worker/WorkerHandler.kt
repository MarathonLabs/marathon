package com.malinskiy.marathon.worker

import org.gradle.workers.WorkerExecutor

interface WorkerHandler {
    fun accept(action: WorkerAction)
    fun startWorker(workerExecutor: WorkerExecutor, parameters: () -> MarathonWorkParameters)
}