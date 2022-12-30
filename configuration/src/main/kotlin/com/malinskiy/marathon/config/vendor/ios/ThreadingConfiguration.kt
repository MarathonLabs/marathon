package com.malinskiy.marathon.config.vendor.ios

data class ThreadingConfiguration(
    val deviceProviderThreads: Int = 8,
    val deviceThreads: Int = 2,
)
