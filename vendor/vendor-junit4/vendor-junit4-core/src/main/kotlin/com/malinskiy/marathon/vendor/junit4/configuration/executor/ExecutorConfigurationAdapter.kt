package com.malinskiy.marathon.vendor.junit4.configuration.executor

import java.io.File

abstract class ExecutorConfigurationAdapter : ExecutorConfiguration {
    override val parallelism: Int = Runtime.getRuntime().availableProcessors()
    override val javaHome: File? = null
    override val javaOptions: List<String> = emptyList()
    override val useArgfiles: Boolean = true
    override val debug: Boolean = false
}
