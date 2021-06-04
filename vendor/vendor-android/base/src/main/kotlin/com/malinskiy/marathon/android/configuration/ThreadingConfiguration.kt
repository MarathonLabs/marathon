package com.malinskiy.marathon.android.configuration

data class ThreadingConfiguration(
    val bootWaitingThreads: Int = 4,
    val adbIoThreads: Int = 4
)
