package com.malinskiy.marathon.worker

import com.malinskiy.marathon.execution.ComponentInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.gradle.workers.WorkerExecutor
import java.util.concurrent.atomic.AtomicBoolean

class WorkerContext : WorkerHandler {

    lateinit var parameters: MarathonWorkParameters

    private val isRunning = AtomicBoolean(false)

    val delayStartChannel = Channel<Unit>()
    val componentsChannel: Channel<ComponentInfo> = Channel(capacity = Channel.UNLIMITED)

    override fun accept(action: WorkerAction): Unit = runBlocking {
        when (action) {
            is WorkerAction.Start -> delayStartChannel.send(Unit)
            is WorkerAction.ScheduleTests -> {
                componentsChannel.offer(action.componentInfo)
            }
            is WorkerAction.Finish -> componentsChannel.close()
        }
    }

    override fun ensureWorkerStarted(
        workerExecutor: WorkerExecutor,
        parameters: () -> MarathonWorkParameters
    ) {
        if (!isRunning.compareAndSet(false, true)) return

        this.parameters = parameters()

        workerExecutor
            .noIsolation()
            .submit(MarathonWorker::class.java) {

            }
    }
}
