package com.malinskiy.marathon.time

interface Timer {
    fun currentTimeMillis(): Long
    fun measure(block: () -> Unit): Long
}