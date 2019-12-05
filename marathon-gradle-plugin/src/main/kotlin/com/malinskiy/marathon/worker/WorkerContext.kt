package com.malinskiy.marathon.worker

import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.execution.Configuration
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

class WorkerContext : WorkerHandler {

    private lateinit var configuration: Configuration
    private val componentsChannel: Channel<ComponentInfo> = Channel(capacity = Channel.UNLIMITED)

    private val isRunning = AtomicBoolean(false)
    private val startedLatch = CountDownLatch(1)

    private lateinit var executor: ExecutorService
    private lateinit var finishFuture: Future<*>

    override fun initialize(configuration: Configuration) {
        this.configuration = configuration
    }

    override fun ensureStarted() {
        if (isRunning.getAndSet(true)) return

        val runnable = WorkerRunnable(componentsChannel, configuration)

        executor = Executors.newSingleThreadExecutor()
        finishFuture = executor.submit(runnable)

        startedLatch.countDown()
    }

    override fun scheduleTests(componentInfo: ComponentInfo) {
        componentsChannel.offer(componentInfo)
    }

    override fun stop() {
        if (isRunning.get()) {
            startedLatch.await(WAITING_FOR_START_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            componentsChannel.close()

            try {
                // Use future to propagate all exceptions from runnable
                finishFuture.get()
            } finally {
                executor.shutdown()
            }
        }
    }

    private companion object {
        private const val WAITING_FOR_START_TIMEOUT_MINUTES = 1L
    }
}
