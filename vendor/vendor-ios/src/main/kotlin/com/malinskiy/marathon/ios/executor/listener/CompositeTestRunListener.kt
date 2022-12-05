package com.malinskiy.marathon.ios.executor.listener

class CompositeTestRunListener(private val listeners: List<IOSTestRunListener>) : IOSTestRunListener {
    private inline fun execute(f: (IOSTestRunListener) -> Unit) {
        listeners.forEach(f)
    }

    override suspend fun afterTestRun() {
        execute { it.afterTestRun() }
    }
}
