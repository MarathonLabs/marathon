package com.malinskiy.marathon.config.vendor.android

data class ThreadingConfiguration(
    val bootWaitingThreads: Int = 4,
    val adbIoThreads: Int = 4
)
