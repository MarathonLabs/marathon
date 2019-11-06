package com.malinskiy.marathon.time

interface Timer {
    val startTimeMillis: Long
    val elapsedTimeMillis: Long

    fun currentTimeMillis(): Long

    fun measure(block: () -> Unit): Long
}