package com.malinskiy.marathon.vendor.junit4.executor

import com.malinskiy.marathon.vendor.junit4.client.TestDiscoveryClient
import com.malinskiy.marathon.vendor.junit4.client.TestExecutorClient
import com.malinskiy.marathon.vendor.junit4.executor.listener.LineListener

interface Booter {
    val testExecutorClient: TestExecutorClient?
    val testDiscoveryClient: TestDiscoveryClient?

    fun prepare()
    fun recreate()
    fun dispose()

    fun addLogListener(listener: LineListener)
    fun removeLogListener(listener: LineListener)
}
