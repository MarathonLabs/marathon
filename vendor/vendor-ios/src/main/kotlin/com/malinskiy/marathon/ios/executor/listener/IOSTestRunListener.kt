package com.malinskiy.marathon.ios.executor.listener

interface IOSTestRunListener {
    suspend fun afterTestRun() {}
}
