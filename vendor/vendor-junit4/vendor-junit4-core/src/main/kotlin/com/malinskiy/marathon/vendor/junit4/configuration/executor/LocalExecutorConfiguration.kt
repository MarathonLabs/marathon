package com.malinskiy.marathon.vendor.junit4.configuration.executor

import java.io.File

class LocalExecutorConfiguration(
    override val parallelism: Int = Runtime.getRuntime().availableProcessors(),
    override val javaHome: File? = null,
    override val javaOptions: List<String> = emptyList(),
    override val useArgfiles: Boolean = false,
    override val debug: Boolean = false,
) : ExecutorConfigurationAdapter()
