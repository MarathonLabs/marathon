package com.malinskiy.marathon.vendor.junit4.configuration.executor

import java.io.File

interface ExecutorConfiguration {
    val parallelism: Int
    val javaHome: File?
    val javaOptions: List<String>
    val useArgfiles: Boolean
    val debug: Boolean
}
