package com.malinskiy.marathon.vendor.junit4.configuration.executor

import com.malinskiy.marathon.vendor.junit4.executor.ExecutionMode
import java.io.File

interface ExecutorConfiguration {
    val parallelism: Int
    val javaHome: File?
    val javaOptions: List<String>
    val useArgfiles: Boolean
    val debug: Boolean
    val mode: ExecutionMode
}
