package com.malinskiy.marathon.config.vendor.android

import java.io.Serializable

data class ThreadingConfiguration(
    val bootWaitingThreads: Int = 4,
    val adbIoThreads: Int = 4
) : Serializable
