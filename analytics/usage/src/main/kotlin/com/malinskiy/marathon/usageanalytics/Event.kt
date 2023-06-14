package com.malinskiy.marathon.usageanalytics

sealed class Event {
    data class TestsTotal(
        val total: Int
    ) : Event()
    
    data class TestsRun(
        val value: Int
    ) : Event()

    data class Devices(
        val total: Int
    ) : Event()

    data class Executed(
        val seconds: Long
    ) : Event()
}
