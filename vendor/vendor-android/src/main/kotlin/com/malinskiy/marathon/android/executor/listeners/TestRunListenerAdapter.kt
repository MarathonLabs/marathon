package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.execution.listener.TestRunListener

class TestRunListenerAdapter(val listener: TestRunListener) : AndroidTestRunListener {
    override suspend fun beforeTestRun(info: InstrumentationInfo?) {
        listener.beforeTestRun()
    }

    override suspend fun testStarted(test: TestIdentifier) {
        listener.testStarted(test.toTest())
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        listener.testEnded(test.toTest())
    }

    override suspend fun afterTestRun() {
        listener.afterTestRun()
    }
}
